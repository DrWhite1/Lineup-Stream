package com.ipvio.stream
import org.junit.Assert.*
import org.junit.Test
class GuideScopeTest {
    private fun ch(id:String,cat:String)=Item(id,Kind.LIVE,id,null,"https://demo.invalid/live/user/fictional/$id.ts",null,cat,providerGroupId=cat)
    @Test fun selectedIdsStayScopedAndRemovedChannelsDoNotFallBackToAll() {
        val a=ch("a","1");val b=ch("b","2")
        try {
            GuideSelection.ids=listOf("a")
            assertEquals(listOf(a),GuideSelection.resolve(listOf(a,b)))
            assertTrue(GuideSelection.resolve(listOf(b)).isEmpty())
            assertEquals(listOf(a),guideCategories(listOf(a),listOf(a,b)).single().items)
        } finally {GuideSelection.ids=null}
    }
    @Test fun firstGuideWithoutSelectionDoesNotExpandToWholeProvider() {
        val a=ch("a","1");val b=ch("b","2");val c=ch("c","1")
        GuideSelection.ids=null
        assertEquals(listOf(a,c),GuideSelection.resolve(listOf(a,b,c)))
    }
}
