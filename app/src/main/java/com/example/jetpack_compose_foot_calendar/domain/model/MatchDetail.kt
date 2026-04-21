/**
 * Aggregated domain model for the match detail screen.
 *
 * A [MatchDetail] is built by combining the results of four parallel API calls: the fixture
 * itself, per-team statistics, the match event timeline, and the team lineups.
 */
package com.example.jetpack_compose_foot_calendar.domain.model

/**
 * Full detail for a single fixture.
 *
 * @property match       Core fixture information (teams, score, status, league).
 * @property statistics  Per-team statistics (shots, possession, passes, etc.).
 * @property events      Ordered timeline of match events (goals, cards, substitutions, VAR).
 * @property lineups     Starting eleven and substitutes for each team.
 */
data class MatchDetail(
    val match: Match,
    val statistics: List<TeamStatistics>,
    val events: List<MatchEvent>,
    val lineups: List<TeamLineup>
)

/**
 * Match statistics for a single team, represented as a key-value map.
 *
 * @property team  The team these statistics belong to.
 * @property stats Map of statistic label (e.g. `"Shots on Goal"`) to its value as a string.
 *                 Values default to an empty string when the API provides `null`.
 */
data class TeamStatistics(
    val team: Team,
    val stats: Map<String, String>
)

/**
 * A single event that occurred during the match.
 *
 * @property minute The match minute at which the event occurred.
 * @property team   The team involved in the event.
 * @property player Display name of the player involved; empty string if unavailable.
 * @property type   Categorised event type.
 * @property detail More specific description of the event (e.g. `"Yellow Card"`, `"Normal Goal"`).
 */
data class MatchEvent(
    val minute: Int,
    val team: Team,
    val player: String,
    val type: EventType,
    val detail: String
)

/**
 * High-level categorisation of a match event.
 *
 * - [GOAL]         A goal was scored (normal, own goal, penalty).
 * - [CARD]         A yellow or red card was shown.
 * - [SUBSTITUTION] A player substitution was made.
 * - [VAR]          A VAR review decision was recorded.
 * - [UNKNOWN]      An event type not yet handled by the mapping layer.
 */
enum class EventType { GOAL, CARD, SUBSTITUTION, VAR, UNKNOWN }

/**
 * The lineup submitted by a team for a given fixture.
 *
 * @property team        The team this lineup belongs to.
 * @property formation   Tactical formation (e.g. `"4-3-3"`).
 * @property startXI     List of players in the starting eleven.
 * @property substitutes Players available on the bench.
 */
data class TeamLineup(
    val team: Team,
    val formation: String,
    val startXI: List<Player>,
    val substitutes: List<Player>
)

/**
 * Represents a player within a lineup.
 *
 * @property id       Unique player identifier.
 * @property name     Player display name.
 * @property number   Shirt number worn during the match.
 * @property position Position code (e.g. `"G"` goalkeeper, `"D"` defender, `"M"` midfielder,
 *                    `"F"` forward). Empty string if not provided by the API.
 */
data class Player(
    val id: Int,
    val name: String,
    val number: Int,
    val position: String
)