/**
 * DTOs for the `/standings` API endpoint.
 *
 * Represents the league table for a given season.
 * Converted to domain [Standing] objects via [Mappers.toDomain].
 */
package com.example.jetpack_compose_foot_calendar.data.api.dto

/**
 * Top-level response wrapper for the `/standings` endpoint.
 *
 * @property response A list containing one entry per requested league.
 */
data class StandingsResponseDto(
    val response: List<StandingResponseDto>
)

/**
 * Container for a single league's standings data.
 *
 * @property league The league object including its nested standings table.
 */
data class StandingResponseDto(
    val league: StandingLeagueDto
)

/**
 * League metadata plus its standings groups.
 *
 * @property id         Unique league identifier.
 * @property name       Display name of the league.
 * @property standings  A list of groups (each group is a list of ranked entries). Regular leagues
 *                      have a single group; cup competitions may have multiple.
 */
data class StandingLeagueDto(
    val id: Int,
    val name: String,
    val standings: List<List<StandingEntryDto>>
)

/**
 * A single team entry in the standings table.
 *
 * @property rank      Current league rank.
 * @property team      Basic team information.
 * @property points    Total points accumulated.
 * @property goalsDiff Goal difference (goals scored minus goals conceded).
 * @property all       Aggregated stats across all matches played.
 */
data class StandingEntryDto(
    val rank: Int,
    val team: TeamDto,
    val points: Int,
    val goalsDiff: Int,
    val all: StandingStatsDto
)

/**
 * Aggregated win/draw/loss record for a team.
 *
 * @property played Total number of matches played.
 * @property win    Number of wins.
 * @property draw   Number of draws.
 * @property lose   Number of losses.
 */
data class StandingStatsDto(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int
)