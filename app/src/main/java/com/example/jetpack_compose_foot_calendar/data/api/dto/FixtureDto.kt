/**
 * Data Transfer Objects (DTOs) for the `/fixtures` API endpoint.
 *
 * These classes mirror the raw JSON structure returned by API-Football and are only used
 * in the data layer. They are converted to domain models via the extension functions in
 * [Mappers].
 */
package com.example.jetpack_compose_foot_calendar.data.api.dto

/**
 * Top-level response wrapper for the `/fixtures` endpoint.
 *
 * @property response The list of fixture entries returned by the API.
 */
data class FixturesResponseDto(
    val response: List<FixtureResponseDto>
)

/**
 * A single fixture entry aggregating all associated data for one match.
 *
 * @property fixture Core fixture metadata (ID, date, status).
 * @property league  League and country information.
 * @property teams   Home and away team references.
 * @property goals   Full-time goal counts (nullable if the match has not been played).
 * @property score   Halftime and fulltime score breakdown.
 */
data class FixtureResponseDto(
    val fixture: FixtureDto,
    val league: LeagueDto,
    val teams: TeamsDto,
    val goals: GoalsDto,
    val score: ScoreDetailDto
)

/**
 * Core fixture metadata.
 *
 * @property id     Unique fixture identifier assigned by API-Football.
 * @property date   ISO-8601 date-time string (e.g. `"2024-04-21T15:00:00+00:00"`).
 * @property status Current match status containing the short code and elapsed minutes.
 */
data class FixtureDto(
    val id: Int,
    val date: String,
    val status: StatusDto
)

/**
 * Match status returned by the API.
 *
 * @property short   Short status code (e.g. `"NS"`, `"1H"`, `"FT"`). Mapped to [MatchStatus] via
 *                   [String.toMatchStatus].
 * @property elapsed Minutes elapsed since kick-off; `null` if the match has not started.
 */
data class StatusDto(
    val short: String,
    val elapsed: Int?
)

/**
 * League metadata associated with a fixture.
 *
 * @property id      Unique league identifier.
 * @property name    Display name of the league (e.g. `"Premier League"`).
 * @property country Country the league belongs to (e.g. `"England"`).
 * @property logo    URL of the league logo image.
 * @property flag    URL of the country flag image; may be `null` for international competitions.
 */
data class LeagueDto(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?
)

/**
 * Container for the home and away team references in a fixture.
 *
 * @property home The home team.
 * @property away The away team.
 */
data class TeamsDto(
    val home: TeamDto,
    val away: TeamDto
)

/**
 * Basic team information.
 *
 * @property id   Unique team identifier.
 * @property name Team display name.
 * @property logo URL of the team badge image.
 */
data class TeamDto(
    val id: Int,
    val name: String,
    val logo: String
)

/**
 * Goal counts for a match or a period.
 *
 * @property home Goals scored by the home team; `null` if not yet determined.
 * @property away Goals scored by the away team; `null` if not yet determined.
 */
data class GoalsDto(
    val home: Int?,
    val away: Int?
)

/**
 * Score breakdown split by period.
 *
 * @property halftime Score at the end of the first half.
 * @property fulltime Score at the end of full time (90 min).
 */
data class ScoreDetailDto(
    val halftime: GoalsDto,
    val fulltime: GoalsDto
)