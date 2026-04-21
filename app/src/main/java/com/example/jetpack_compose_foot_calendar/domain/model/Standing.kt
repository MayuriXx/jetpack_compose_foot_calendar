package com.example.jetpack_compose_foot_calendar.domain.model

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
