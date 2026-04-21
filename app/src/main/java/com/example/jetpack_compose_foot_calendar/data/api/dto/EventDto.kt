package com.example.jetpack_compose_foot_calendar.data.api.dto

data class EventsResponseDto(
    val response: List<MatchEventDto>
)

data class MatchEventDto(
    val time: EventTimeDto,
    val team: TeamDto,
    val player: EventPlayerDto,
    val type: String,
    val detail: String
)

data class EventTimeDto(
    val elapsed: Int,
    val extra: Int?
)

data class EventPlayerDto(
    val id: Int?,
    val name: String?
)