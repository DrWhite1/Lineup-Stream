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
