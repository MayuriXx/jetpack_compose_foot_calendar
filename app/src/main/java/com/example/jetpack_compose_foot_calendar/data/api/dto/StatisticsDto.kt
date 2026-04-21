package com.example.jetpack_compose_foot_calendar.data.api.dto

data class StatisticsResponseDto(
    val response: List<TeamStatisticsDto>
)

data class TeamStatisticsDto(
    val team: TeamDto,
    val statistics: List<StatisticItemDto>
)

data class StatisticItemDto(
    val type: String,
    val value: String?
)