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

}
