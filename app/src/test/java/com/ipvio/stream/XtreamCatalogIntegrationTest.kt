package com.ipvio.stream
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringWriter

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class XtreamCatalogIntegrationTest {
    @Test fun apiImportHasLiveMoviesSeriesAndLazyEpisodesEvenWhenGetPhpIsBlocked() {
        val server=MockWebServer()
        val vodCalls=java.util.concurrent.atomic.AtomicInteger()
        server.dispatcher=object:Dispatcher() { override fun dispatch(request:RecordedRequest):MockResponse {
            if(request.path!!.startsWith("/get.php")) return MockResponse().setResponseCode(403)
            val action=request.requestUrl!!.queryParameter("action")
            if(action=="get_vod_streams" && vodCalls.incrementAndGet()==1) return MockResponse().setResponseCode(503)
            val body=when(action) {
                null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                "get_live_categories" -> """[{"category_id":10,"category_name":"Sports"}]"""
                "get_live_streams" -> """[{"stream_id":1,"name":"Arena One","category_id":10,"epg_channel_id":"arena"}]"""
                "get_vod_categories" -> """[{"category_id":20,"category_name":"Movies"}]"""
                "get_vod_streams" -> """[{"stream_id":2,"name":"Demo Film","category_ids":[20],"container_extension":"mkv"}]"""
                "get_series_categories" -> """[{"category_id":30,"category_name":"Series"}]"""
                "get_series" -> """[{"series_id":3,"name":"Demo Show","category_id":30}]"""
                "get_series_info" -> """{"episodes":{"1":[{"id":4,"episode_num":1,"container_extension":"mp4"}]}}"""
                else -> "{}"
            }
            return MockResponse().setBody(body)
        } }
        server.start()
        try {
            val account=XtreamLogic.Acct(server.url("/").toString().trimEnd('/'),"fictional-user","fictional-password")
            val api=XtreamApi(OkHttpClient(),"LineupStream-test")
            val writer=StringWriter();assertEquals(3,api.write(account,writer))
            val catalog=M3uParser.parseStream(writer.toString().reader())
            assertEquals(2,vodCalls.get())
            assertEquals(setOf(Kind.LIVE,Kind.MOVIE,Kind.SERIES),catalog.items.map { it.kind }.toSet())
            assertEquals("10",catalog.items.single { it.kind==Kind.LIVE }.providerGroupId)
            assertEquals("arena",catalog.items.single { it.kind==Kind.LIVE }.tvgId)
            assertTrue(catalog.items.single { it.kind==Kind.MOVIE }.url.endsWith("2.mkv"))
            val eps=api.episodeList(account,"3")!!
            assertEquals(1,eps.single().season);assertEquals(1,eps.single().num)
            assertTrue(eps.single().url.endsWith("4.mp4"))
        } finally { server.shutdown() }
    }
}
