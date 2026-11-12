# European football sources

The sports hub uses unofficial public ESPN JSON endpoints for scores, fixtures and team data. EPG/channel matching is separate and depends on subscription metadata; a fixture does not guarantee a watchable channel. No API key is needed. The endpoints can change or fail.

Verified on 12 November 2026: each supported endpoint returned the named league identity, not merely an HTTP 200. Existing big-five and Champions/Europa League paths were preserved.

- UEFA Nations League: https://site.api.espn.com/apis/site/v2/sports/soccer/uefa.nations/scoreboard
- Belgian Pro League: https://site.api.espn.com/apis/site/v2/sports/soccer/bel.1/scoreboard
- Dutch Eredivisie: https://site.api.espn.com/apis/site/v2/sports/soccer/ned.1/scoreboard
- Portuguese Primeira Liga: https://site.api.espn.com/apis/site/v2/sports/soccer/por.1/scoreboard
- Scottish Premiership: https://site.api.espn.com/apis/site/v2/sports/soccer/sco.1/scoreboard
- Turkish Super Lig: https://site.api.espn.com/apis/site/v2/sports/soccer/tur.1/scoreboard
- Swiss Super League: https://site.api.espn.com/apis/site/v2/sports/soccer/sui.1/scoreboard
- Austrian Bundesliga: https://site.api.espn.com/apis/site/v2/sports/soccer/aut.1/scoreboard
- Danish Superliga: https://site.api.espn.com/apis/site/v2/sports/soccer/den.1/scoreboard
- Norwegian Eliteserien: https://site.api.espn.com/apis/site/v2/sports/soccer/nor.1/scoreboard
- Swedish Allsvenskan: https://site.api.espn.com/apis/site/v2/sports/soccer/swe.1/scoreboard
- Greek Super League: https://site.api.espn.com/apis/site/v2/sports/soccer/gre.1/scoreboard
- UEFA Conference League: https://site.api.espn.com/apis/site/v2/sports/soccer/uefa.europa.conf/scoreboard
- English League Championship: https://site.api.espn.com/apis/site/v2/sports/soccer/eng.2/scoreboard
- Spanish LALIGA 2: https://site.api.espn.com/apis/site/v2/sports/soccer/esp.2/scoreboard
- Italian Serie B: https://site.api.espn.com/apis/site/v2/sports/soccer/ita.2/scoreboard
- French Ligue 2: https://site.api.espn.com/apis/site/v2/sports/soccer/fra.2/scoreboard
- English FA Cup: https://site.api.espn.com/apis/site/v2/sports/soccer/eng.fa/scoreboard
- Spanish Copa del Rey: https://site.api.espn.com/apis/site/v2/sports/soccer/esp.copa_del_rey/scoreboard

The bel.1 feed is displayed as Jupiler Pro League, the user's term for Belgian Pro League. These two priority competitions appear first in the league browser and overview picks.

Polish pol.1 returned HTTP 400 and was not added. Concurrent league scoreboard requests are limited to four. Per-league cached refresh rules remain as upstream. Two public one-event JSON fixtures are retained in test resources for parser regression tests; no private provider data is included.
