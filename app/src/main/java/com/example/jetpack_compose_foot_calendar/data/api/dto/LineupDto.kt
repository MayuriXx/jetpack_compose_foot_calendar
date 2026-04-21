package com.example.jetpack_compose_foot_calendar.data.api.dto

data class LineupsResponseDto(
    val response: List<TeamLineupDto>
)

data class TeamLineupDto(
    val team: TeamDto,
    val formation: String,
    val startXI: List<LineupPlayerDto>,
    val substitutes: List<LineupPlayerDto>
)

data class LineupPlayerDto(
    val player: LineupPlayerDetailDto
)

data class LineupPlayerDetailDto(
    val id: Int,
    val name: String,
    val number: Int,
    val pos: String?
)