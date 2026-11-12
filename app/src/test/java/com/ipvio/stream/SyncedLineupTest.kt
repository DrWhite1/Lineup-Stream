package com.ipvio.stream
import org.junit.Assert.*
import org.junit.Test
class SyncedLineupTest {
    private fun ch(id:String,group:String="Sport",cat:String="10",server:String="provider.example",password:String="secret")=Item(id,Kind.LIVE,id,null,"https://$server/live/user/$password/$id.ts",null,group,providerGroupId=cat)
    @Test fun addsAndRemovesProviderMembersWithoutSnapshots() {
        val a=ch("1");val g=GroupRule(ChannelListLogic.groupKey(a),"Sport")
        val l=ChList(1,"My lineup",emptyList(),listOf(g))
        assertEquals(listOf(a),ChannelListLogic.members(l,g.key,listOf(a)))
        val b=ch("2"); assertEquals(listOf(b),ChannelListLogic.members(l,g.key,listOf(b)))
        assertTrue(ChannelListLogic.members(l,g.key,emptyList()).isEmpty())
        assertEquals(listOf(g),l.groups)
    }
    @Test fun providerRenameAndPasswordChangeKeepGroupIdentity() {
        assertEquals(ChannelListLogic.groupKey(ch("1")),ChannelListLogic.groupKey(ch("2","New Sport",password="changed")))
        assertNotEquals(ChannelListLogic.groupKey(ch("1")),ChannelListLogic.groupKey(ch("1",server="other.example")))
        assertNotEquals(ChannelListLogic.groupKey(ch("1")),ChannelListLogic.groupKey(ch("1",cat="11")))
        val nested=ch("1").copy(url="https://provider.example/sub/live/user/password/1.ts")
        assertNotEquals(ChannelListLogic.groupKey(ch("1")),ChannelListLogic.groupKey(nested))
        assertNotEquals(ChannelListLogic.groupKey(nested),ChannelListLogic.groupKey(nested.copy(url="https://provider.example/sub/live/other/password/1.ts")))
    }
    @Test fun individualChannelsRemainSeparateAndUnionDoesNotDuplicate() {
        val a=ch("1");val b=ch("2",cat="20")
        val l=ChList(1,"My lineup",listOf(a.id,b.id),listOf(GroupRule(ChannelListLogic.groupKey(a),"Sport")))
        assertEquals(listOf(a,b),ChannelListLogic.resolve(l,mapOf(a.id to a,b.id to b)))
        assertEquals(listOf(a,b),ChannelListLogic.members(l,"#individual",listOf(a,b)))
        assertEquals(listOf(a),ChannelListLogic.members(l,l.groups.first().key,listOf(a,b)))
    }
    @Test fun providerCategoryIdSurvivesCacheAndHiddenEntrySkip() {
        val a=ch("1").copy(lang="de");val b=ch("2",cat="20").copy(lang="en")
        val out=java.io.ByteArrayOutputStream()
        PlaylistCacheIO.write(out,"demo",listOf(a,b),emptyList(),emptyMap())
        val read=PlaylistCacheIO.read(java.io.ByteArrayInputStream(out.toByteArray()),"demo",setOf("de"))!!
        assertEquals(listOf(b),read.items)
        assertEquals("20",read.items.single().providerGroupId)
    }
    @Test fun rulesRoundTripAndOldListsMigrate() {
        val l=ChList(2,"Sport",listOf("1"),listOf(GroupRule("scope:id:1","News, sport & more")))
        assertEquals(listOf(l),ChannelListLogic.decode(ChannelListLogic.encode(listOf(l))))
        assertEquals(listOf(ChList(1,"Old",listOf("a","b"))),ChannelListLogic.decode("1\tOld\ta,b"))
        assertEquals(listOf(l),ChannelListLogic.addGroup(listOf(l),2,l.groups.first()))
        assertTrue(ChannelListLogic.removeGroup(listOf(l),2,l.groups.first().key).first().groups.isEmpty())
    }
}
