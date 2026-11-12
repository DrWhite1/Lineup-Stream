package com.ipvio.stream
import org.junit.Assert.*
import org.junit.Test
class EuropeanSoccerTest {
    @Test fun majorEuropeanCompetitionsHaveDistinctFeeds() {
        val names=listOf("Jupiler Pro League","UEFA Nations League","Premier League","LaLiga","Serie A","Bundesliga","Ligue 1","Eredivisie","Champions League","Europa League","Conference League","Primeira Liga","Scottish Premiership","Turkish Super Lig")
        val leagues=names.map { SportData.league(it)!! }
        assertEquals(names.size,leagues.map {it.path}.distinct().size)
        assertTrue(leagues.all {it.sport=="soccer" && it.kind==SportData.KIND_TEAM})
        assertEquals(SportData.LEAGUES.size,SportData.LEAGUES.map {it.path}.distinct().size)
    }
    @Test fun belgianAndEuropeanGuideTitlesAreRecognized() {
        listOf("UEFA Nations League: Belgium - France","Jupiler Pro League: Gent - Genk","Belgian Pro League","Eredivisie","Liga Portugal","Scottish Premiership","Conference League","Allsvenskan","Eliteserien","Süper Lig").forEach {
            assertTrue(it,SportLogic.isSportEvent(it))
            assertEquals(it,SportLogic.catIndex("Fußball"),SportLogic.categoryOf(it,""))
        }
        assertFalse(SportLogic.isSportEvent("Transport documentary"))
    }
    @Test fun verifiedEspnPriorityFixturesParseIntoTheRightCompetitions() {
        listOf("bel.1" to "Jupiler Pro League","uefa.nations" to "UEFA Nations League").forEach { (code,name) ->
            val text=javaClass.getResourceAsStream("/espn-$code.json")!!.bufferedReader().readText()
            val games=SportData.parse(text,SportData.league(name)!!)
            assertTrue(name,games.isNotEmpty())
            assertTrue(games.all {it.league==name && it.home.name.isNotBlank() && it.away.name.isNotBlank()})
        }
    }
}
