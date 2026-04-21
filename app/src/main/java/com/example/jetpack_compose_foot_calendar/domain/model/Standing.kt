package com.example.jetpack_compose_foot_calendar.domain.model

/**
 * Represents a single entry in a league standings table.
 *
 * @property rank      The team's current position in the league.
 * @property team      Basic team information (name, logo).
 * @property points    Total points accumulated during the season.
 * @property played    Total number of matches played.
 * @property won       Number of matches won.
 * @property drawn     Number of matches drawn.
 * @property lost      Number of matches lost.
 * @property goalsDiff Goal difference (goals scored minus goals conceded).
 */
data class Standing(
    val rank: Int,
    val team: Team,
    val points: Int,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsDiff: Int
)
