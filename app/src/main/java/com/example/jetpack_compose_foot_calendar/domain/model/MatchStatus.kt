package com.example.jetpack_compose_foot_calendar.domain.model

/**
 * Represents the current status of a football match.
 *
 * API-Football short codes are mapped to these values by [String.toMatchStatus]:
 *
 * | Status        | API short codes                          |
 * |---------------|------------------------------------------|
 * | [LIVE]        | `1H`, `2H`, `HT`, `ET`, `P`, `LIVE`     |
 * | [UPCOMING]    | `NS`, `TBD`                              |
 * | [FINISHED]    | `FT`, `AET`, `PEN`                       |
 * | [UNKNOWN]     | Any other code                           |
 */
enum class MatchStatus {
    /** The match is currently in progress (first half, second half, half-time, extra time, penalties). */
    LIVE,

    /** The match has not started yet or the kick-off time is to be determined. */
    UPCOMING,

    /** The match has ended (full time, after extra time, or after penalties). */
    FINISHED,

    /** The status code returned by the API is not recognised by the app. */
    UNKNOWN
}