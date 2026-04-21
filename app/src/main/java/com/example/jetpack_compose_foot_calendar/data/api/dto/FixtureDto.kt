package com.example.jetpack_compose_foot_calendar.data.api.dto

data class FixturesResponseDto(
    val response: List<FixtureResponseDto>
)

data class FixtureResponseDto(
    val fixture: FixtureDto,
    val league: LeagueDto,
    val teams: TeamsDto,
    val goals: GoalsDto,
    val score: ScoreDetailDto
)

data class FixtureDto(
    val id: Int,
    val date: String,
    val status: StatusDto
)

data class StatusDto(
    val short: String,
    val elapsed: Int?
)

data class LeagueDto(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?
)

data class TeamsDto(
    val home: TeamDto,
    val away: TeamDto
)

data class TeamDto(
    val id: Int,
    val name: String,
    val logo: String
)

data class GoalsDto(
    val home: Int?,
    val away: Int?
)

data class ScoreDetailDto(
    val halftime: GoalsDto,
    val fulltime: GoalsDto
)