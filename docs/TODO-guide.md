# Guide loading & channel list TODO

Notes based on the current codebase (Lineup-Stream fork of IPVIO Stream).

## Implemented first slice (0.2.2)

- Catalog changes and phone-import updates no longer start XMLTV downloads. Short now/next is requested only by rendered channel UI / the currently playing channel; the full grid uses per-channel data for visible rows and its focused channel.
- Short EPG uses its cache first, then Xtream, then manually loaded XMLTV as fallback. Duplicate same-channel calls share a fetch; empty results have a 15-second retry cooldown. Transient network/HTTP failures get one retry.
- Explicit XMLTV load stays in Settings. It keeps the active selection's channel IDs and skips other programme subtrees before reading title/description. It still downloads the full provider file when no filtered endpoint exists. M3U-only providers must use manual XMLTV loading.
- Existing now/next lines, progress bars, scoped grid and hold-to-scroll protection remain.
- 359 tests passed including a 10,000-channel synthetic catalog with requests only for two requested channels, no catalog-triggered guide request, cache/dedup/retry, and manual XMLTV filtering.

Not implemented in this slice: conditional XMLTV downloads (ETag/If-Modified-Since), separate periodic guide worker, new timeline UI, provider-side category/day filtering, and R8 changes. The checklist below remains historical suggestions, not a claim that every item is complete.

## Prior state (before 0.2.2)

- Channel rows already show **now + next + progress bar** when data is available (`ChannelLine` in `LiveTab.kt`).
- Data comes from `Epg.now()` (with a short delay + hold-to-scroll protection so it doesn't thrash while zapping).
- Bulk XMLTV is loaded via `Xmltv.start()` when the playlist changes (`PlaylistStore`). It builds a `wanted` set from the live channels' `tvg-id`s and only keeps programmes for those, with a per-channel cap (`MAX_PER_CHANNEL = 80`) and a memory-tight early exit.
- Richer on-demand data for the full grid lives in `EpgFull.get()` (local XMLTV first, then Xtream `get_simple_data_table`, then fallback).
- The separate TV-Guide screen is already scoped to the active list/group (`GuideSelection`).

## Suggested TODO

### Guide loading (high priority — large channel count problem)

- [ ] Make the XMLTV parse skip channels outside the active lineup/group earlier (or better: prefer a provider endpoint that can be filtered by category / day).
- [ ] Add If-Modified-Since / ETag support so unchanged guides aren't fully re-downloaded.
- [ ] Investigate Xtream `get_short_epg` / `get_simple_data_table` as the primary path for visible channels instead of (or before) the full XMLTV dump. Fall back to XMLTV only for deeper history.
- [ ] Surface truncation / memory-tight status more clearly in the UI (currently only a status string).
- [ ] Optional: split guide by day or by active group so a large provider doesn't force a multi-GB parse every time.

### Channel list integration

- [ ] Ensure now/next on rows prefers cached short-EPG data immediately and fills in from the bulk XMLTV in the background (so rows aren't blank until the full parse finishes).
- [ ] Consider a compact timeline strip or "next" line that doesn't require opening the full Guide screen.
- [ ] Keep the existing hold-to-scroll protection so zapping stays smooth.

### Background updates

- [ ] Add a periodic background refresh (WorkManager or similar) for the guide, independent of the manual playlist refresh. Respect playback so it doesn't interrupt.
- [ ] Retry on transient network errors (currently mostly one-shot).

### Navigation / polish

- [ ] Review focus handling in the full guide grid (`PlayerGuide.kt`) for remote feel.
- [ ] Make sure switching groups/lineups updates the now/next on the visible rows without a full reload.

### Lower priority

- [ ] ProGuard/R8 notes (there's already an ADR and a `_to_delete_keepRules` folder — check if a release build is planned).
- [ ] Progress feedback during the bulk XMLTV load (status exists, but UI could show it more prominently).
