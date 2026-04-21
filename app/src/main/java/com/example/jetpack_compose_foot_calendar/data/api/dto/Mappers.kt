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

fun String.toMatchStatus(): MatchStatus {
    return when (this) {
        "1H", "2H", "HT", "ET", "P", "LIVE" -> MatchStatus.LIVE
        "NS", "TBD" -> MatchStatus.UPCOMING
        "FT", "AET", "PEN" -> MatchStatus.FINISHED
        else -> MatchStatus.UNKNOWN
    }
}

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

fun TeamStatisticsDto.toDomain(): TeamStatistics {
    return TeamStatistics(
        team = Team(id = team.id, name = team.name, logo = team.logo),
        stats = statistics.associate { it.type to (it.value ?: "") }
    )
}

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

fun TeamLineupDto.toDomain(): TeamLineup {
    return TeamLineup(
        team = Team(id = team.id, name = team.name, logo = team.logo),
        formation = formation,
        startXI = startXI.map { it.player.toDomain() },
        substitutes = substitutes.map { it.player.toDomain() }
    )
}

fun LineupPlayerDetailDto.toDomain(): Player {
    return Player(
        id = id,
        name = name,
        number = number,
        position = pos ?: ""
    )
}
