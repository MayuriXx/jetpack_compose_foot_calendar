/**
 * DTOs for the `/fixtures/events` API endpoint.
 *
 * Represents match events such as goals, cards, substitutions and VAR decisions.
 * Converted to domain [MatchEvent] objects via [Mappers.toDomain].
 */
package com.example.jetpack_compose_foot_calendar.data.api.dto

/**
 * Top-level response wrapper for the `/fixtures/events` endpoint.
 *
 * @property response Ordered list of events that occurred during the match.
 */
data class EventsResponseDto(
    val response: List<MatchEventDto>
)

/**
 * A single match event (goal, card, substitution, VAR decision, etc.).
 *
 * @property time   The minute at which the event occurred.
 * @property team   The team involved in the event.
 * @property player The player who triggered the event (ID and name may be null for some event types).
 * @property type   High-level event type string returned by the API (e.g. `"Goal"`, `"Card"`).
 * @property detail A more precise description (e.g. `"Normal Goal"`, `"Yellow Card"`).
 */
data class MatchEventDto(
    val time: EventTimeDto,
    val team: TeamDto,
    val player: EventPlayerDto,
    val type: String,
    val detail: String
)

/**
 * Timing information for a match event.
 *
 * @property elapsed  Regular match minute (1–90+).
 * @property extra    Extra-time minute offset; `null` during regular play.
 */
data class EventTimeDto(
    val elapsed: Int,
    val extra: Int?
)

/**
 * Player reference within an event.
 *
 * Both fields may be `null` when the API does not provide player details for the event.
 *
 * @property id   Player identifier; `null` if unavailable.
 * @property name Player display name; `null` if unavailable.
 */
data class EventPlayerDto(
    val id: Int?,
    val name: String?
)