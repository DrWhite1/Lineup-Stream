package com.ipvio.stream

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.SemanticsActions
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w1280dp-h720dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LineupRenderTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    private fun snap(name:String) {
        compose.waitForIdle()
        val dir=File(System.getProperty("user.dir"),"build/lineup-screenshots");dir.mkdirs()
        File(dir,"$name.png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun lineupOpensGroupsBeforeChannels() {
        val sport=Item("demo1",Kind.LIVE,"Arena One",null,"https://example.invalid/live/demo/fictional/1.ts",null,"Sports",tvgId="arena",providerGroupId="10")
        val news=Item("demo2",Kind.LIVE,"News One",null,"https://example.invalid/live/demo/fictional/2.ts",null,"News",providerGroupId="20")
        val field=Xmltv::class.java.getDeclaredField("progs").apply { isAccessible=true }
        @Suppress("UNCHECKED_CAST")
        val programmes=field.get(Xmltv) as java.util.concurrent.ConcurrentHashMap<String,List<EpgEntry>>
        val now=System.currentTimeMillis()/1000
        programmes["arena"]=listOf(EpgEntry("Demo match - live","Fictional guide programme for visual testing",now-900,now+2700),EpgEntry("Sports roundup","Fictional next programme",now+2700,now+6300))
        val ctx=compose.activity
        ChannelLists.init(ctx)
        val id=ChannelLists.create(ctx,"My evening")
        ChannelLists.addGroup(ctx,id,GroupRule(ChannelListLogic.groupKey(sport),"Sports"))
        ChannelLists.add(ctx,id,news.id)
        compose.setContent { Box(Modifier.fillMaxSize().background(Color(0xFF0B101B))) { LiveTab(listOf(sport,news),mapOf("Sports" to listOf(sport),"News" to listOf(news)),emptyList(),onOpen={}) } }
        snap("01-live-home")
        compose.onNodeWithText("My evening").performScrollTo().performClick()
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        snap("02-lineup-groups")
        compose.onNodeWithText("My evening · Open a group",useUnmergedTree=true).assertExists()
        compose.onNodeWithText("Individual channels",useUnmergedTree=true).assertExists()
        // Sports also exists in sidebar. The group row has its own distinct group panel text parent.
        compose.onAllNodesWithText("Sports",useUnmergedTree=true).onLast().performClick()
        compose.mainClock.advanceTimeBy(500)
        snap("03-group-channels")
        compose.onNodeWithText("Back to lineup groups",useUnmergedTree=true).assertExists()
        compose.onAllNodesWithText("Arena One",useUnmergedTree=true).onFirst().assertExists()
        compose.onNodeWithText("TV guide").performClick()
        compose.runOnIdle {
            org.junit.Assert.assertEquals(listOf(sport.id),ZapContext.list.map {it.id})
            org.junit.Assert.assertTrue(AppNav.openGuide)
            AppNav.openGuide=false
        }
    }
    @Test fun playerBrowserUsesOpenableGroupsToo() {
        val sport=Item("demo1",Kind.LIVE,"Arena One",null,"https://example.invalid/live/demo/fictional/1.ts",null,"Sports",providerGroupId="10")
        val ctx=compose.activity
        ChannelLists.init(ctx)
        val id=ChannelLists.create(ctx,"Player lineup")
        ChannelLists.addGroup(ctx,id,GroupRule(ChannelListLogic.groupKey(sport),"Sports"))
        compose.setContent {
            var cat by remember { mutableStateOf(ChannelListLogic.PREFIX+id) }
            var cats by remember { mutableStateOf(false) }
            var header by remember { mutableStateOf(false) }
            var focused by remember { mutableStateOf<Item?>(sport) }
            Box(Modifier.fillMaxSize().background(Color(0xFF0B101B))) {
                ChannelOverlay(listOf(sport),sport.id,sport,cat,{cat=it},cats,{cats=it},focused,{focused=it},header,{header=it},remember{FocusRequester()},remember{FocusRequester()},remember{FocusRequester()},{},{_,_->})
            }
        }
        snap("04-player-lineup-groups")
        compose.onNodeWithText("Open a group",useUnmergedTree=true).assertExists()
        compose.onNodeWithText("Sports",useUnmergedTree=true).performClick()
        snap("05-player-group-channels")
        compose.onAllNodesWithText("Arena One",useUnmergedTree=true).onFirst().assertExists()
        compose.onNodeWithText("Back to lineup groups",useUnmergedTree=true).assertExists()
    }

    @Test fun scopedGuideRendersTimeGridWithoutUnrelatedChannels() {
        val now=System.currentTimeMillis()/1000
        val field=Xmltv::class.java.getDeclaredField("progs").apply { isAccessible=true }
        @Suppress("UNCHECKED_CAST")
        val programmes=field.get(Xmltv) as java.util.concurrent.ConcurrentHashMap<String,List<EpgEntry>>
        val channels=(1..20).map { n ->
            programmes["guide$n"]=(0..4).map { t -> EpgEntry("Demo programme $n.$t","Fictional programme description",now-1800+t*3600,now+1800+t*3600) }
            Item("guide$n",Kind.LIVE,"Channel $n",null,"https://example.invalid/$n.ts",null,"My Sports",tvgId="guide$n")
        }
        LiveHold.lastRepeat=-10000
        @Suppress("UNCHECKED_CAST")
        val cache=EpgFull::class.java.getDeclaredField("cache").apply { isAccessible=true }.get(EpgFull) as LruCache<String,Pair<Long,List<EpgEntry>>>
        channels.forEach { cache[it.id]=System.currentTimeMillis() to programmes[it.tvgId].orEmpty() }
        GuideSelection.name="My lineup / Sports"
        compose.setContent { Guide(channels,channels.first().id,remember{FocusRequester()},{},all=channels+Item("other",Kind.LIVE,"Unrelated News",null,"https://example.invalid/other",null,"Other"),onPick={}) }
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithText("Unrelated News",useUnmergedTree=true).assertDoesNotExist()
        compose.onNodeWithText("My lineup / Sports · 20 channels",useUnmergedTree=true).assertExists()
        compose.onAllNodesWithText("Demo programme 1.0",useUnmergedTree=true).onFirst().assertExists()
        snap("06-scoped-guide")
        compose.onNodeWithText("2h >").performClick()
        snap("07-guide-forward")
    }

    @Test fun soccerLeaguesPageShowsPriorityCompetitions() {
        compose.setContent { Box(Modifier.fillMaxSize().background(Color(0xFF0B101B))) { HubLeagues(HubCtx(remember{HubNav()},emptyList(),{_,_->},{_,_->})) } }
        compose.onNodeWithText("UEFA Nations League",useUnmergedTree=true).assertExists()
        compose.onNodeWithText("Jupiler Pro League",useUnmergedTree=true).assertExists()
        snap("08-european-leagues")
    }

}
