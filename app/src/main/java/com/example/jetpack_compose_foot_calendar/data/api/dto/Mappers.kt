/**
 * Mapping functions that convert API DTOs to domain model objects.
 *
 * All functions are stateless extension functions and do not depend on any external state.
 * They form the anti-corruption layer between the raw API responses and the rest of the app.
 */
package com.example.jetpack_compose_foot_calendar.data.api.dto

import com.example.jetpack_compose_foot_calendar.domain.model.EventType
import com.example.jetpack_compose_foot_calendar.domain.model.League
import com.example.jetpack_compose_foot_calendar.domain.model.Match
import com.example.jetpack_compose_foot_calendar.domain.model.MatchEvent
import com.example.jetpack_compose_foot_calendar.domain.model.MatchStatus
import com.example.jetpack_compose_foot_calendar.domain.model.Player
import com.example.jetpack_compose_foot_calendar.domain.model.Score
import com.example.jetpack_compose_foot_calendar.domain.model.Standing
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import com.example.jetpack_compose_foot_calendar.domain.model.TeamLineup
import com.example.jetpack_compose_foot_calendar.domain.model.TeamStatistics

/**
 * Converts a [FixtureResponseDto] to a [Match] domain model.
 *
 * @receiver The raw fixture response from the API.
 * @return   A fully populated [Match] instance.
 */
fun FixtureResponseDto.toDomain(): Match {
    return Match(
        fixtureId = fixture.id,
        date = fixture.date,
        status = fixture.status.short.toMatchStatus(),
        homeTeam = Team(id = teams.home.id, name = teams.home.name, logo = teams.home.logo),
        awayTeam = Team(id = teams.away.id, name = teams.away.name, logo = teams.away.logo),
        score = Score(home = goals.home, away = goals.away),
        league = League(
            id = league.id,
            name = league.name,
            country = league.country,
            logo = league.logo,
            flag = league.flag
        )
    )
}

/**
 * Converts an API-Football short status code to a [MatchStatus] enum value.
 *
 * | API codes              | Result                    |
 * |------------------------|---------------------------|
 * | `1H`, `2H`, `HT`, `ET`, `P`, `LIVE` | [MatchStatus.LIVE]     |
 * | `NS`, `TBD`            | [MatchStatus.UPCOMING]    |
 * | `FT`, `AET`, `PEN`     | [MatchStatus.FINISHED]    |
 * | anything else          | [MatchStatus.UNKNOWN]     |
 *
 * @receiver The short code string as returned by the API (e.g. `"FT"`, `"1H"`).
 * @return   The corresponding [MatchStatus].
 */
fun String.toMatchStatus(): MatchStatus {
    return when (this) {
        "1H", "2H", "HT", "ET", "P", "LIVE" -> MatchStatus.LIVE
        "NS", "TBD" -> MatchStatus.UPCOMING
        "FT", "AET", "PEN" -> MatchStatus.FINISHED
        else -> MatchStatus.UNKNOWN
    }
}

/**
 * Converts a [StandingEntryDto] to a [Standing] domain model.
 *
 * @receiver The raw standing entry from the API.
 * @return   A fully populated [Standing] instance.
 */
fun StandingEntryDto.toDomain(): Standing {
    return Standing(
        rank = rank,
        team = Team(id = team.id, name = team.name, logo = team.logo),
        points = points,
        played = all.played,
        won = all.win,
        drawn = all.draw,
        lost = all.lose,
        goalsDiff = goalsDiff
    )
}

/**
 * Converts a [TeamStatisticsDto] to a [TeamStatistics] domain model.
 *
 * Statistics are stored as a [Map] keyed by the statistic type label. Null values from the
 * API are replaced with an empty string.
 *
 * @receiver The raw team statistics from the API.
 * @return   A [TeamStatistics] instance with the statistics map.
 */
fun TeamStatisticsDto.toDomain(): TeamStatistics {
    return TeamStatistics(
        team = Team(id = team.id, name = team.name, logo = team.logo),
        stats = statistics.associate { it.type to (it.value ?: "") }
    )
}

/**
 * Converts a [MatchEventDto] to a [MatchEvent] domain model.
 *
 * The `type` string from the API is case-insensitively mapped to an [EventType] enum value.
 * Unknown types fall back to [EventType.UNKNOWN].
 *
 * @receiver The raw match event from the API.
 * @return   A fully populated [MatchEvent] instance.
 */
fun MatchEventDto.toDomain(): MatchEvent {
    return MatchEvent(
        minute = time.elapsed,
        team = Team(id = team.id, name = team.name, logo = team.logo),
        player = player.name ?: "",
        type = when (type.lowercase()) {
            "goal" -> EventType.GOAL
            "card" -> EventType.CARD
            "subst" -> EventType.SUBSTITUTION
            "var" -> EventType.VAR
            else -> EventType.UNKNOWN
        },
        detail = detail
    )
}

/**
 * Converts a [TeamLineupDto] to a [TeamLineup] domain model.
 *
 * @receiver The raw team lineup from the API.
 * @return   A [TeamLineup] with mapped starting XI and substitutes.
 */
fun TeamLineupDto.toDomain(): TeamLineup {
    return TeamLineup(
        team = Team(id = team.id, name = team.name, logo = team.logo),
        formation = formation,
        startXI = startXI.map { it.player.toDomain() },
        substitutes = substitutes.map { it.player.toDomain() }
    )
}

/**
 * Converts a [LineupPlayerDetailDto] to a [Player] domain model.
 *
 * A null position is replaced with an empty string.
 *
 * @receiver The raw player detail from the lineup response.
 * @return   A fully populated [Player] instance.
 */
fun LineupPlayerDetailDto.toDomain(): Player {
    return Player(
        id = id,
        name = name,
        number = number,
        position = pos ?: ""
    )
}
