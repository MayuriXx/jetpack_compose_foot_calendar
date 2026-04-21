/**
 * Core domain models for a football fixture.
 *
 * These are plain Kotlin data classes with no dependency on the Android framework or the
 * data layer. They are the only model types consumed by ViewModels and UI composables.
 */
package com.example.jetpack_compose_foot_calendar.domain.model

/**
 * Represents a single football fixture (match).
 *
 * @property fixtureId Unique identifier assigned by API-Football.
 * @property date      ISO-8601 date-time string of the kick-off time.
 * @property status    Current status of the match (live, upcoming, finished, unknown).
 * @property homeTeam  The team playing at home.
 * @property awayTeam  The team playing away.
 * @property score     Current or final goal counts; values are `null` before the match starts.
 * @property league    The league and country this fixture belongs to.
 */
data class Match(
    val fixtureId: Int,
    val date: String,
    val status: MatchStatus,
    val homeTeam: Team,
    val awayTeam: Team,
    val score: Score,
    val league: League
)

/**
 * Represents a football club.
 *
 * @property id   Unique team identifier.
 * @property name Display name of the team.
 * @property logo URL of the team's badge image.
 */
data class Team(
    val id: Int,
    val name: String,
    val logo: String
)

/**
 * Represents the goal score for both sides of a match.
 *
 * @property home Goals scored by the home team; `null` if the match has not started.
 * @property away Goals scored by the away team; `null` if the match has not started.
 */
data class Score(
    val home: Int?,
    val away: Int?
)

/**
 * Represents the league (competition) associated with a fixture.
 *
 * @property id      Unique league identifier.
 * @property name    Display name of the league (e.g. `"Ligue 1"`).
 * @property country Country or confederation the league belongs to.
 * @property logo    URL of the league logo image.
 * @property flag    URL of the country flag image; `null` for international competitions.
 */
data class League(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?
)