# 0.2.2 validation

- assembleDebug and testDebugUnitTest passed. 359 tests, zero failures/errors/skips.
- Mock-provider tests: catalog-change handler makes zero requests with a 10,000-channel synthetic catalog; only two requested channels call get_short_epg; duplicate calls share cache/fetch; one simulated HTTP 503 is retried; HTTP 403 uses a brief empty-result cache; no XMLTV request is triggered by API failure.
- Manual XMLTV parser test skips 5,000 unrelated programme subtrees and retains only the requested channel. Nested irrelevant title markup is skipped without extracting its text.
- Verified nested Xtream base paths and percent-encoded credentials are preserved. No real provider credentials were used.
- Re-rendered/inspected actual group/channel and scoped-guide UI. Layout unchanged; demo programmes fictional. Source inspection confirms only the explicit Settings XMLTV buttons call Xmltv.start.
- Same debug package/signing certificate, versionCode 9. No clean-lint claim; actual provider/TV acceptance remains necessary.

# 0.2.1 validation

- assembleDebug passed; 355 tests passed with zero failures/errors/skips, including guide scope resolution, removed-channel no-ALL-fallback, route checks, scoped guide/time navigation renders and European football feed/EPG tests.
- Live ESPN identity checks passed for 19 new European football feeds including the user's priorities Jupiler Pro League and UEFA Nations League. Parser tests use one real public event from each priority feed. This validates feed integration, not future availability or broadcast rights.
- Inspected 1280x720 component renders of the scoped time grid, focused programme panel, two-hour navigation and league browser. Guide demo channels/programmes are fictional.
- Lint was not repeated; the prior incomplete lint result still applies. Actual TV remote behavior, provider EPG data and movie/series playback/resume still need the user's device test.

# 0.2.0 validation

- `assembleDebug`: passed using JDK 25, Gradle 9.6, Android SDK 37.
- `testDebugUnitTest`: 348 tests, zero failures/errors/skips. This includes the upstream tests, synced-group persistence and cache tests, mock Xtream API live/movie/series imports and episode retrieval, and two Compose/Robolectric render tests.
- Mock API includes a blocked get.php and a first-attempt HTTP 503 for movie streams, followed by sequential retry. No paid provider account was used.
- Rendered and visually checked 1280x720 Live TV lineup groups, opened channels with current/next programme data, and player channel browser. Images in screenshots use fictional demo channels and programmes, not an actual provider feed. These are component renders, not proof of TV hardware playback.
- Source whitespace check and token/private-key pattern scan passed.
- Lint was attempted twice but did not finish within the available execution window (the first attempt also exhausted its heap). No clean lint result is claimed. See upstream's technical-debt documentation for known issues.

## Device acceptance still required

Install this separate debug-signed app, enter the subscription on-device and test: live tune/zap, movie playback and resume, series seasons/episodes and resume, EPG supplied by the provider, remote focus/back, and refresh after provider group membership changes. Initial provider import is not promised to be instant. Screenshots and mock-provider tests do not verify compatibility with any particular subscription.

Debug application ID: `be.jeroen.lineupstream.test`. Android 7.0+ (API 24). The debug signing key is not included in source. This is a test APK, not a store-signed release.
