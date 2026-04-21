package com.example.jetpack_compose_foot_calendar.domain.model

data class MatchDetail(
    val match: Match,
    val statistics: List<TeamStatistics>,
    val events: List<MatchEvent>,
    val lineups: List<TeamLineup>
)


data class TeamStatistics(
    val team: Team,
    val stats: Map<String, String>
)

data class MatchEvent(
    val minute: Int,
    val team: Team,
    val player: String,
    val type: EventType,
    val detail: String
)

enum class EventType { GOAL, CARD, SUBSTITUTION, VAR, UNKNOWN }

data class TeamLineup(
    val team: Team,
    val formation: String,
    val startXI: List<Player>,
    val substitutes: List<Player>
)

data class Player(
    val id: Int,
    val name: String,
    val number: Int,
    val position: String
)