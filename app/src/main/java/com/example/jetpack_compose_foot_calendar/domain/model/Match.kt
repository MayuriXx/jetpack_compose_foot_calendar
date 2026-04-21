package com.example.jetpack_compose_foot_calendar.domain.model

data class Match(
    val fixtureId: Int,
    val date: String,
    val status: MatchStatus,
    val homeTeam: Team,
    val awayTeam: Team,
    val score: Score,
    val league: League
)

data class Team(
    val id: Int,
    val name: String,
    val logo: String
)

data class Score(
    val home: Int?,
    val away: Int?
)

data class League(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?
)