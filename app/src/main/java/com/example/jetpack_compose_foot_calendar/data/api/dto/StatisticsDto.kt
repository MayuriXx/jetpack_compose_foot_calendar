/**
 * DTOs for the `/fixtures/statistics` API endpoint.
 *
 * Represents per-team match statistics (shots, possession, passes, etc.).
 * Converted to domain [TeamStatistics] objects via [Mappers.toDomain].
 */
package com.example.jetpack_compose_foot_calendar.data.api.dto

/**
 * Top-level response wrapper for the `/fixtures/statistics` endpoint.
 *
 * @property response A list containing one statistics block per team (two entries per fixture).
 */
data class StatisticsResponseDto(
    val response: List<TeamStatisticsDto>
)

/**
 * Statistics for a single team in a fixture.
 *
 * @property team       The team these statistics belong to.
 * @property statistics A flat list of typed statistic entries.
 */
data class TeamStatisticsDto(
    val team: TeamDto,
    val statistics: List<StatisticItemDto>
)

/**
 * A single statistic entry identified by a type label.
 *
 * @property type  Human-readable statistic label (e.g. `"Shots on Goal"`, `"Ball Possession"`).
 * @property value The statistic value as a string; `null` when the API does not provide the value.
 */
data class StatisticItemDto(
    val type: String,
    val value: String?
)