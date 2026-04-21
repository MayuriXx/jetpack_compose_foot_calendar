package com.example.jetpack_compose_foot_calendar.data.api.dto

data class StandingsResponseDto(
    val response: List<StandingResponseDto>
)

data class StandingResponseDto(
    val league: StandingLeagueDto
)

data class StandingLeagueDto(
    val id: Int,
    val name: String,
    val standings: List<List<StandingEntryDto>>
)

data class StandingEntryDto(
    val rank: Int,
    val team: TeamDto,
    val points: Int,
    val goalsDiff: Int,
    val all: StandingStatsDto
)

data class StandingStatsDto(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int
)