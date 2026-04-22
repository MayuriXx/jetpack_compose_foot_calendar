package com.example.jetpack_compose_foot_calendar.data.api.dto

import com.example.jetpack_compose_foot_calendar.domain.model.EventType
import com.example.jetpack_compose_foot_calendar.domain.model.League
import com.example.jetpack_compose_foot_calendar.domain.model.MatchStatus
import com.example.jetpack_compose_foot_calendar.domain.model.Player
import com.example.jetpack_compose_foot_calendar.domain.model.Score
import com.example.jetpack_compose_foot_calendar.domain.model.Standing
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import com.example.jetpack_compose_foot_calendar.domain.model.TeamLineup
import com.example.jetpack_compose_foot_calendar.domain.model.TeamStatistics
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests unitaires pour toutes les fonctions de mapping définies dans Mappers.kt.
 *
 * Ces fonctions sont pures (stateless), ce qui les rend idéales pour des tests simples
 * sans mocking ni coroutines.
 */
class MappersTest {

    // ─── String.toMatchStatus() ───────────────────────────────────────────────

    @Test
    fun `toMatchStatus - FT retourne FINISHED`() {
        assertEquals(MatchStatus.FINISHED, "FT".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - AET retourne FINISHED`() {
        assertEquals(MatchStatus.FINISHED, "AET".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - PEN retourne FINISHED`() {
        assertEquals(MatchStatus.FINISHED, "PEN".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - NS retourne UPCOMING`() {
        assertEquals(MatchStatus.UPCOMING, "NS".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - TBD retourne UPCOMING`() {
        assertEquals(MatchStatus.UPCOMING, "TBD".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - 1H retourne LIVE`() {
        assertEquals(MatchStatus.LIVE, "1H".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - 2H retourne LIVE`() {
        assertEquals(MatchStatus.LIVE, "2H".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - HT retourne LIVE`() {
        assertEquals(MatchStatus.LIVE, "HT".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - ET retourne LIVE`() {
        assertEquals(MatchStatus.LIVE, "ET".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - P retourne LIVE`() {
        assertEquals(MatchStatus.LIVE, "P".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - LIVE retourne LIVE`() {
        assertEquals(MatchStatus.LIVE, "LIVE".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - code inconnu retourne UNKNOWN`() {
        assertEquals(MatchStatus.UNKNOWN, "XYZ".toMatchStatus())
    }

    @Test
    fun `toMatchStatus - chaîne vide retourne UNKNOWN`() {
        assertEquals(MatchStatus.UNKNOWN, "".toMatchStatus())
    }

    // ─── FixtureResponseDto.toDomain() ────────────────────────────────────────

    private fun buildFixtureDto(
        id: Int = 1001,
        date: String = "2025-10-05T15:00:00+00:00",
        statusShort: String = "FT",
        homeId: Int = 10, homeName: String = "PSG", homeLogo: String = "https://psg.logo",
        awayId: Int = 20, awayName: String = "OM",  awayLogo: String = "https://om.logo",
        goalsHome: Int? = 2, goalsAway: Int? = 1,
        leagueId: Int = 61, leagueName: String = "Ligue 1",
        leagueCountry: String = "France", leagueLogo: String = "https://ligue1.logo",
        leagueFlag: String? = "https://france.flag"
    ) = FixtureResponseDto(
        fixture = FixtureDto(id = id, date = date, status = StatusDto(short = statusShort, elapsed = null)),
        teams = TeamsDto(
            home = TeamDto(id = homeId, name = homeName, logo = homeLogo),
            away = TeamDto(id = awayId, name = awayName, logo = awayLogo)
        ),
        goals = GoalsDto(home = goalsHome, away = goalsAway),
        league = LeagueDto(id = leagueId, name = leagueName, country = leagueCountry, logo = leagueLogo, flag = leagueFlag),
        score = ScoreDetailDto(halftime = GoalsDto(null, null), fulltime = GoalsDto(goalsHome, goalsAway))
    )

    @Test
    fun `FixtureResponseDto toDomain - mappe correctement le fixture terminé`() {
        val dto = buildFixtureDto()
        val result = dto.toDomain()

        assertEquals(1001, result.fixtureId)
        assertEquals("2025-10-05T15:00:00+00:00", result.date)
        assertEquals(MatchStatus.FINISHED, result.status)
        assertEquals(Team(id = 10, name = "PSG", logo = "https://psg.logo"), result.homeTeam)
        assertEquals(Team(id = 20, name = "OM", logo = "https://om.logo"), result.awayTeam)
        assertEquals(Score(home = 2, away = 1), result.score)
        assertEquals(
            League(id = 61, name = "Ligue 1", country = "France", logo = "https://ligue1.logo", flag = "https://france.flag"),
            result.league
        )
    }

    @Test
    fun `FixtureResponseDto toDomain - mappe un match à venir avec score null`() {
        val dto = buildFixtureDto(statusShort = "NS", goalsHome = null, goalsAway = null)
        val result = dto.toDomain()

        assertEquals(MatchStatus.UPCOMING, result.status)
        assertEquals(Score(home = null, away = null), result.score)
    }

    @Test
    fun `FixtureResponseDto toDomain - flag de ligue nullable`() {
        val dto = buildFixtureDto(leagueFlag = null)
        assertEquals(null, dto.toDomain().league.flag)
    }

    // ─── StandingEntryDto.toDomain() ──────────────────────────────────────────

    @Test
    fun `StandingEntryDto toDomain - mappe correctement une entrée de classement`() {
        val dto = StandingEntryDto(
            rank = 1,
            team = TeamDto(id = 85, name = "Paris Saint-Germain", logo = "https://psg.logo"),
            points = 72,
            goalsDiff = 45,
            all = StandingStatsDto(played = 30, win = 23, draw = 3, lose = 4)
        )

        val result = dto.toDomain()

        assertEquals(
            Standing(
                rank = 1,
                team = Team(id = 85, name = "Paris Saint-Germain", logo = "https://psg.logo"),
                points = 72,
                played = 30,
                won = 23,
                drawn = 3,
                lost = 4,
                goalsDiff = 45
            ),
            result
        )
    }

    // ─── TeamStatisticsDto.toDomain() ─────────────────────────────────────────

    @Test
    fun `TeamStatisticsDto toDomain - mappe les statistiques en map clé-valeur`() {
        val dto = TeamStatisticsDto(
            team = TeamDto(id = 10, name = "PSG", logo = "https://psg.logo"),
            statistics = listOf(
                StatisticItemDto("Shots on Goal", "5"),
                StatisticItemDto("Ball Possession", "60%")
            )
        )

        val result = dto.toDomain()

        assertEquals(Team(id = 10, name = "PSG", logo = "https://psg.logo"), result.team)
        assertEquals(
            TeamStatistics(
                team = Team(id = 10, name = "PSG", logo = "https://psg.logo"),
                stats = mapOf("Shots on Goal" to "5", "Ball Possession" to "60%")
            ),
            result
        )
    }

    @Test
    fun `TeamStatisticsDto toDomain - valeur null remplacée par chaîne vide`() {
        val dto = TeamStatisticsDto(
            team = TeamDto(id = 10, name = "PSG", logo = ""),
            statistics = listOf(StatisticItemDto("Fouls", null))
        )

        val result = dto.toDomain()

        assertEquals("", result.stats["Fouls"])
    }

    @Test
    fun `TeamStatisticsDto toDomain - liste de stats vide donne map vide`() {
        val dto = TeamStatisticsDto(
            team = TeamDto(id = 10, name = "PSG", logo = ""),
            statistics = emptyList()
        )

        assertEquals(emptyMap<String, String>(), dto.toDomain().stats)
    }

    // ─── MatchEventDto.toDomain() ─────────────────────────────────────────────

    private fun buildEventDto(type: String, playerName: String? = "Mbappé", minute: Int = 33) =
        MatchEventDto(
            time = EventTimeDto(elapsed = minute, extra = null),
            team = TeamDto(id = 10, name = "PSG", logo = ""),
            player = EventPlayerDto(id = 7, name = playerName),
            type = type,
            detail = "Normal Goal"
        )

    @Test
    fun `MatchEventDto toDomain - type Goal mappe vers GOAL`() {
        assertEquals(EventType.GOAL, buildEventDto("Goal").toDomain().type)
    }

    @Test
    fun `MatchEventDto toDomain - type goal en minuscule mappe vers GOAL`() {
        assertEquals(EventType.GOAL, buildEventDto("goal").toDomain().type)
    }

    @Test
    fun `MatchEventDto toDomain - type Card mappe vers CARD`() {
        assertEquals(EventType.CARD, buildEventDto("Card").toDomain().type)
    }

    @Test
    fun `MatchEventDto toDomain - type subst mappe vers SUBSTITUTION`() {
        assertEquals(EventType.SUBSTITUTION, buildEventDto("subst").toDomain().type)
    }

    @Test
    fun `MatchEventDto toDomain - type Var mappe vers VAR`() {
        assertEquals(EventType.VAR, buildEventDto("Var").toDomain().type)
    }

    @Test
    fun `MatchEventDto toDomain - type inconnu mappe vers UNKNOWN`() {
        assertEquals(EventType.UNKNOWN, buildEventDto("PenaltyMissed").toDomain().type)
    }

    @Test
    fun `MatchEventDto toDomain - nom de joueur null remplacé par chaîne vide`() {
        val event = buildEventDto("Goal", playerName = null)
        assertEquals("", event.toDomain().player)
    }

    @Test
    fun `MatchEventDto toDomain - minute correctement mappée`() {
        val event = buildEventDto("Goal", minute = 77)
        assertEquals(77, event.toDomain().minute)
    }

    // ─── TeamLineupDto.toDomain() ─────────────────────────────────────────────

    @Test
    fun `TeamLineupDto toDomain - mappe la formation et les joueurs`() {
        val dto = TeamLineupDto(
            team = TeamDto(id = 10, name = "PSG", logo = "https://psg.logo"),
            formation = "4-3-3",
            startXI = listOf(
                LineupPlayerDto(LineupPlayerDetailDto(id = 1, name = "Donnarumma", number = 99, pos = "G")),
                LineupPlayerDto(LineupPlayerDetailDto(id = 7, name = "Mbappé", number = 7, pos = "F"))
            ),
            substitutes = listOf(
                LineupPlayerDto(LineupPlayerDetailDto(id = 25, name = "Safonov", number = 25, pos = "G"))
            )
        )

        val result = dto.toDomain()

        assertEquals(
            TeamLineup(
                team = Team(id = 10, name = "PSG", logo = "https://psg.logo"),
                formation = "4-3-3",
                startXI = listOf(
                    Player(id = 1, name = "Donnarumma", number = 99, position = "G"),
                    Player(id = 7, name = "Mbappé", number = 7, position = "F")
                ),
                substitutes = listOf(
                    Player(id = 25, name = "Safonov", number = 25, position = "G")
                )
            ),
            result
        )
    }

    // ─── LineupPlayerDetailDto.toDomain() ─────────────────────────────────────

    @Test
    fun `LineupPlayerDetailDto toDomain - position null remplacée par chaîne vide`() {
        val dto = LineupPlayerDetailDto(id = 1, name = "Donnarumma", number = 99, pos = null)
        assertEquals("", dto.toDomain().position)
    }

    @Test
    fun `LineupPlayerDetailDto toDomain - tous les champs correctement mappés`() {
        val dto = LineupPlayerDetailDto(id = 7, name = "Mbappé", number = 7, pos = "F")
        assertEquals(Player(id = 7, name = "Mbappé", number = 7, position = "F"), dto.toDomain())
    }

    // ─── TeamEntryDto.toDomain() ──────────────────────────────────────────────

    @Test
    fun `TeamEntryDto toDomain - mappe correctement vers Team`() {
        val dto = TeamEntryDto(team = TeamDto(id = 42, name = "Stade Rennais", logo = "https://rennes.logo"))
        assertEquals(Team(id = 42, name = "Stade Rennais", logo = "https://rennes.logo"), dto.toDomain())
    }
}

