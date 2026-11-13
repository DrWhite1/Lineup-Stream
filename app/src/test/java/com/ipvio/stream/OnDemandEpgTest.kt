package com.ipvio.stream

import kotlinx.coroutines.*
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class OnDemandEpgTest {
    private fun ch(server:MockWebServer,id:Int)=Item("on-demand-$id",Kind.LIVE,"Channel $id",null,server.url("/sub/live/fictional-user/fictional-password/$id.ts").toString(),null,"Group $id",tvgId="epg$id")
    private fun listing(title:String):String {
        val now=System.currentTimeMillis()/1000
        return """{"epg_listings":[{"title":"$title","description":"demo","start_timestamp":"${now-600}","stop_timestamp":"${now+3600}"}]}"""
    }
    @Test fun catalogChangeDoesNotFetchXmltvAndVisibleChannelsUseShortEpgWithCache()=runBlocking {
        val server=MockWebServer();val requests=java.util.concurrent.CopyOnWriteArrayList<String>()
        val attempts=AtomicInteger()
        server.dispatcher=object:okhttp3.mockwebserver.Dispatcher(){override fun dispatch(r:RecordedRequest):MockResponse {
            requests.add(r.path.orEmpty())
            assertEquals("get_short_epg",r.requestUrl!!.queryParameter("action"))
            if(attempts.incrementAndGet()==1) return MockResponse().setResponseCode(503)
            return MockResponse().setBody(listing("Short EPG"))
        }}
        server.start()
        try {
            val catalog=(1..10000).map {ch(server,it)}
            Xmltv.onPlaylistChanged()
            assertEquals(0,server.requestCount)
            val visible=listOf(catalog[17],catalog[8191])
            val results=coroutineScope { (visible+visible).map {async {Epg.now(it)}}.awaitAll() }
            assertTrue(results.all {it.single().title=="Short EPG"})
            assertEquals(3,server.requestCount) // two visible channels, one bounded transient retry
            assertEquals(setOf("18","8192"),requests.map {it.substringAfter("stream_id=").substringBefore('&')}.toSet())
            assertNotNull(Epg.peek(visible[0]));assertEquals(results[0],Epg.now(visible[0]))
            Xmltv.onPlaylistChanged();assertNull(Epg.peek(visible[0]));assertEquals(3,server.requestCount)
        } finally {server.shutdown();Epg.trim()}
    }
    @Test fun failuresAreBrieflyCachedWithoutStartingBulkDownload()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(403))
            val item=ch(server,99001);Epg.trim()
            assertTrue(Epg.now(item).isEmpty());assertTrue(Epg.now(item).isEmpty())
            assertEquals(1,server.requestCount)
        } finally {server.shutdown();Epg.trim()}
    }
    @Test fun manualXmltvSkipsUnselectedProgrammeSubtreesBeforeReadingTitle() {
        val server=MockWebServer();server.start()
        try {
            val now=System.currentTimeMillis()/1000
            val format=java.text.SimpleDateFormat("yyyyMMddHHmmss Z").apply {timeZone=java.util.TimeZone.getTimeZone("UTC")}
            fun t(n:Long)=format.format(java.util.Date(n*1000))
            val unwanted=(1..5000).joinToString("") { "<programme channel=\"other$it\" start=\"${t(now)}\" stop=\"${t(now+3600)}\"><title><nested>Unwanted</nested></title></programme>" }
            server.enqueue(MockResponse().setBody("<tv>$unwanted<programme channel=\"wanted\" start=\"${t(now)}\" stop=\"${t(now+3600)}\"><title>Wanted programme</title><desc>demo</desc></programme></tv>"))
            val out=HashMap<String,MutableList<EpgEntry>>()
            Xmltv.parse(server.url("/manual.xml").toString(),setOf("wanted"),out)
            assertEquals(setOf("wanted"),out.keys);assertEquals("Wanted programme",out["wanted"]!!.single().title)
        } finally {server.shutdown()}
    }
    @Test fun xtreamBasePathAndEncodedCredentialsArePreserved() {
        val item=Item("path",Kind.LIVE,"demo",null,"https://demo.invalid/provider/live/test%40user/pass%26word/42.ts",null,"demo")
        assertEquals("https://demo.invalid/provider/player_api.php?username=test%40user&password=pass%26word&action=get_short_epg&stream_id=42",Epg.xtreamApi(item,"get_short_epg"))
        assertNull(Epg.xtreamApi(item.copy(url="https://demo.invalid/channel.m3u8"),"get_short_epg"))
    }
}
