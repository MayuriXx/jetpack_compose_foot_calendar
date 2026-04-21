/**
 * DTOs for the `/fixtures/lineups` API endpoint.
 *
 * Represents the starting eleven and substitutes for each team in a fixture.
 * Converted to domain [TeamLineup] objects via [Mappers.toDomain].
 */
package com.example.jetpack_compose_foot_calendar.data.api.dto

/**
 * Top-level response wrapper for the `/fixtures/lineups` endpoint.
 *
 * @property response A list containing one entry per team (typically two entries per fixture).
 */
data class LineupsResponseDto(
    val response: List<TeamLineupDto>
)

/**
 * The lineup for a single team.
 *
 * @property team        Basic team information.
 * @property formation   Tactical formation string (e.g. `"4-3-3"`).
 * @property startXI     List of starting players (typically 11 entries).
 * @property substitutes List of players on the bench.
 */
data class TeamLineupDto(
    val team: TeamDto,
    val formation: String,
    val startXI: List<LineupPlayerDto>,
    val substitutes: List<LineupPlayerDto>
)

/**
 * Wrapper around player details as returned by the lineup endpoint.
 *
 * @property player Full player details.
 */
data class LineupPlayerDto(
    val player: LineupPlayerDetailDto
)

/**
 * Detailed player information within a lineup.
 *
 * @property id     Unique player identifier.
 * @property name   Player display name.
 * @property number Shirt number worn during the match.
 * @property pos    Position code (e.g. `"G"`, `"D"`, `"M"`, `"F"`); `null` if not provided.
 */
data class LineupPlayerDetailDto(
    val id: Int,
    val name: String,
    val number: Int,
    val pos: String?
)