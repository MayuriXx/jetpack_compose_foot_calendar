# ⚽ Football Calendar

An Android application built with **Jetpack Compose** that displays today's football fixtures,
allows filtering by status/country/league, and provides a detailed view for each match.

---

## Tech Stack

| Library / Tool | Role |
|---|---|
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | Declarative UI |
| [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) | In-app navigation |
| [ViewModel + StateFlow](https://developer.android.com/topic/libraries/architecture/viewmodel) | UI state management |
| [Retrofit 2](https://square.github.io/retrofit/) + OkHttp | REST API client |
| [Gson](https://github.com/google/gson) | JSON serialisation |
| [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) | Local cache persistence |
| [Coil](https://coil-kt.github.io/coil/) | Async image loading (team logos, flags) |
| [Material 3](https://m3.material.io/) | Design system |
| [API-Football](https://www.api-sports.io/) (`api-sports.io`) | Live football data source |

---

## Architecture

The project follows a clean layered architecture:

```
domain/model          ← Pure Kotlin data classes (Match, Team, League, MatchDetail, …)
       ↑
data/api              ← Retrofit service + DTOs + Mappers (DTO → domain)
data/cache            ← TTL-based DataStore cache (CacheManager)
data/repository       ← Single source of truth, cache-first strategy (FootballRepository)
       ↑
ui/common             ← Shared UiState<T> sealed class
ui/auth               ← AuthViewModel + LoginScreen
ui/calendar           ← CalendarViewModel + CalendarScreen
ui/matchdetail        ← MatchDetailViewModel (screen pending)
ui/profile            ← ProfileViewModel (screen pending)
ui/theme              ← Material 3 colour, typography, theme
       ↑
navigation            ← AppNavGraph + Routes
       ↑
MainActivity          ← Single activity entry point
```

---

## Project Structure

```
app/src/main/java/com/example/jetpack_compose_foot_calendar/
├── MainActivity.kt                        # App entry point, DataStore & Repository setup
├── navigation/
│   └── AppNavGraph.kt                     # NavHost, route constants, auth guard
├── data/
│   ├── api/
│   │   ├── FootballApiService.kt          # Retrofit interface (fixtures, events, lineups, standings)
│   │   ├── RetrofitClient.kt              # OkHttp + Retrofit singleton
│   │   └── dto/
│   │       ├── FixtureDto.kt              # /fixtures response DTOs
│   │       ├── EventDto.kt                # /fixtures/events response DTOs
│   │       ├── LineupDto.kt               # /fixtures/lineups response DTOs
│   │       ├── StatisticsDto.kt           # /fixtures/statistics response DTOs
│   │       ├── StandingDto.kt             # /standings response DTOs
│   │       └── Mappers.kt                 # DTO → domain extension functions
│   ├── cache/
│   │   └── CacheManager.kt               # TTL cache on top of DataStore
│   └── repository/
│       └── FootballRepository.kt          # Cache-first data access, Result<T> wrapper
├── domain/model/
│   ├── Match.kt                           # Match, Team, Score, League
│   ├── MatchDetail.kt                     # MatchDetail, TeamStatistics, MatchEvent, TeamLineup, Player
│   ├── MatchStatus.kt                     # LIVE / UPCOMING / FINISHED / UNKNOWN enum
│   ├── Standing.kt                        # League table entry
│   └── User.kt                            # Authenticated user model
└── ui/
    ├── common/
    │   └── UiState.kt                     # Loading / Success<T> / Error sealed class
    ├── auth/
    │   ├── AuthViewModel.kt               # Simulated login / logout state
    │   └── LoginScreen.kt                 # Login form composable
    ├── calendar/
    │   ├── CalendarViewModel.kt           # Today's fixtures + reactive filter chain
    │   └── CalendarScreen.kt              # Calendar composable (FilterBar, MatchCard, LeagueHeader)
    ├── matchdetail/
    │   └── MatchDetailViewModel.kt        # Match detail state (screen pending)
    ├── profile/
    │   └── ProfileViewModel.kt            # Favourite team state (screen pending)
    └── theme/
        ├── Color.kt                        # Material 3 light/dark colour tokens
        ├── Type.kt                         # Typography configuration
        └── Theme.kt                        # JetPackComposeFootCalendarTheme composable
```

---

## Setup

### Prerequisites

- Android Studio **Hedgehog** or later
- Android SDK with **minSdk 26** (Android 8.0)
- A free API key from [api-sports.io](https://dashboard.api-football.com/register)

### Configuration

1. Copy the template file and fill in your credentials:

```bash
cp local.properties.template local.properties
```

2. Edit `local.properties`:

```properties
sdk.dir=/path/to/your/Android/sdk
FOOTBALL_API_KEY=your_api_key_here
FOOTBALL_API_HOST=https://v3.football.api-sports.io/
```

> ⚠️ `FOOTBALL_API_HOST` **must** include `https://` and a trailing `/`.

3. Sync Gradle and run the app on an emulator or physical device (API 26+).

---

## Caching Strategy

| Data | Cache key | TTL |
|---|---|---|
| Today's fixtures | `matches_today` | 60 min |
| Match detail (live) | `match_detail_<id>` | 5 min |
| Match detail (upcoming / finished) | `match_detail_<id>` | 120 min |
| League standings | `standings_<leagueId>` | 30 min |

Cache entries are serialised to JSON via Gson and stored in DataStore Preferences. A stale
entry is silently discarded and the repository falls back to a fresh API call.

---

## Features

### ✅ Implemented

- **Login screen** — simulated authentication (any username, password ≥ 3 chars)
- **Calendar screen** — today's fixtures loaded from API-Football, grouped by league
- **Filter bar** — filter matches by status (live / upcoming / finished), country, and league
- **Match card** — shows team badges, names, score or kick-off time, and a live indicator
- **League header** — displays country flag, league logo, and names as section separators

### 🚧 In Progress / Pending

- **Match detail screen** — ViewModel ready, composable not yet implemented
- **Profile screen** — ViewModel ready, composable not yet implemented
- **Real authentication** — currently mocked, no backend integration
- **Favourite team persistence** — `ProfileViewModel` stores selection in memory only

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you
would like to change.
