# Fork decision and acceptance checklist

Evaluated upstream commit `62846c75a0950112b2084c92e2998ca325a32b34` (12 November 2026).

## Decision

Extend IPVIO rather than rebuild the first Lineup TV interface. The first prototype proved basic provider playback but did not meet its owner's navigation expectations, and its VOD catalogs failed in the owner's test. IPVIO supplies a complete focus/navigation, VOD, metadata and live-guide UI that would take much more work to reproduce and tune than adding the missing group-rule model.

This is a pragmatic product decision, not a claim that IPVIO is small or architecturally pristine. At evaluation, the source was roughly 40,000 Kotlin lines in 184 files in one package. State is largely global singletons with Compose state, no ViewModel layer or database; player and settings files are large. It has substantial pure-logic tests, streaming JSON imports, memory budgeting, lazy series episodes and explicit focus handling. Upstream's own audit records inherited security and test debt. Keep the fork delta narrow and take upstream updates deliberately.

## Three reported navigation problems

1. **All channels immediately populate Live TV.** Upstream opens a Live HOME view, not ALL. Channel rows use LazyColumn. This fork also builds group/genre indexes off the UI thread. The full first provider import still needs the catalogs; no claim of instant initial import or lower measured memory is made.
2. **Lineups dump every channel instead of groups.** Not fixed by upstream out of the box: upstream's custom channel lists held individual IDs and were flat. This fork adds persistent synced group rules and openable group entries, plus a separate individual-channel entry. Stable Xtream category IDs pass through its M3U bridge and versioned binary cache.
3. **No guide when browsing a channel group.** Upstream channel rows already show current/next, with delayed background lookups and a focused programme panel. The fork reuses those channel rows for an opened lineup group. Programme content still depends on provider EPG support and mapping; a UI alone cannot supply missing guide data.

## VOD and no VPN

Xtream API catalog loading is present for live, movie and series categories, movie streams, series placeholders and lazy season/episode expansion. The fork prefers player_api.php directly for Xtream input, bypassing providers that block or return partial get.php playlists and preserving category IDs. The initial import can still be slow for very large catalogs. User-selected language and content filters can exclude VOD; test them deliberately.

The app needs no VPN and does not bundle a VPN service. Codec, stream format, provider authentication variations, EPG and actual remote navigation require a device test. Local mock-provider tests cannot prove an individual subscription works.

## Before treating a test build as accepted

- Import the same provider on the TV, with live/movies/series enabled.
- Open Movies, confirm posters/titles appear, play a movie.
- Open a series, load a season, play an episode and test resume.
- Open Live, select a group, confirm now/next and programme panel populate.
- Add two groups to a named lineup; open the lineup and confirm it shows group entries, not their combined channels.
- Open each group and check D-pad focus, Back to groups, EPG and channel playback.
- Add an individual channel from another group and confirm it appears under Individual channels.
- Refresh; verify additions/removals by the provider follow the group rule without reselection.

Retain the GPL license, author credits and corresponding source for the modified binary. A different app identity and signing certificate mean this is a separate installation, not an update to official IPVIO or the first Lineup TV prototype.
