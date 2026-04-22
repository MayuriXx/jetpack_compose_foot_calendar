package com.example.jetpack_compose_foot_calendar.data.repository

import com.example.jetpack_compose_foot_calendar.data.api.FootballApiService
import com.example.jetpack_compose_foot_calendar.data.api.dto.EventPlayerDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.EventTimeDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.EventsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.FixtureDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.FixtureResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.FixturesResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.GoalsDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.LeagueDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.LineupPlayerDetailDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.LineupPlayerDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.LineupsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.MatchEventDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.ScoreDetailDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StatisticItemDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StatisticsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingEntryDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingLeagueDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingStatsDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StatusDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.TeamDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.TeamEntryDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.TeamLineupDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.TeamStatisticsDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.TeamsDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.TeamsResponseDto
import com.example.jetpack_compose_foot_calendar.data.cache.CacheManager
import com.example.jetpack_compose_foot_calendar.domain.model.League
import com.example.jetpack_compose_foot_calendar.domain.model.Match
import com.example.jetpack_compose_foot_calendar.domain.model.MatchDetail
import com.example.jetpack_compose_foot_calendar.domain.model.MatchStatus
import com.example.jetpack_compose_foot_calendar.domain.model.Score
import com.example.jetpack_compose_foot_calendar.domain.model.Standing
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour [FootballRepository].
 *
 * La stratégie cache-first est testée pour chaque méthode publique :
 * - Cache disponible → retour immédiat, aucun appel API
 * - Cache absent    → appel API, mise en cache, retour du résultat
 * - Erreur API      → propagation via Result.failure
 *
 * [FootballApiService] et [CacheManager] sont mockés avec MockK (relaxed = true).
 *
 * **Note d'implémentation** : [CacheManager.get] est une fonction `inline reified`
 * qui délègue à [CacheManager.getInternal]. Pour contrôler le cache dans les tests
 * en cache-hit, `getInternal` est stubbé avec `any<Class<*>>()` et un cast `as Any?`.
 * Pour les cas cache-miss, `relaxed = true` retourne `null` automatiquement.
 */
class FootballRepositoryTest {

    private lateinit var api: FootballApiService
    private lateinit var cache: CacheManager
    private lateinit var repo: FootballRepository

    // ─── Fixtures réutilisables ────────────────────────────────────────────────

    private val teamDto    = TeamDto(id = 85, name = "PSG", logo = "https://psg.logo")
    private val teamDomain = Team(id = 85, name = "PSG", logo = "https://psg.logo")

    private fun buildFixtureResponseDto(statusShort: String = "FT") = FixtureResponseDto(
        fixture = FixtureDto(id = 999, date = "2025-10-05T15:00:00+00:00", status = StatusDto(short = statusShort, elapsed = null)),
        teams   = TeamsDto(home = teamDto, away = TeamDto(id = 20, name = "OM", logo = "")),
        goals   = GoalsDto(home = 2, away = 1),
        league  = LeagueDto(id = 61, name = "Ligue 1", country = "France", logo = "", flag = null),
        score   = ScoreDetailDto(halftime = GoalsDto(null, null), fulltime = GoalsDto(2, 1))
    )

    private fun buildMatchDomain(status: MatchStatus = MatchStatus.FINISHED) = Match(
        fixtureId = 999,
        date      = "2025-10-05T15:00:00+00:00",
        status    = status,
        homeTeam  = teamDomain,
        awayTeam  = Team(id = 20, name = "OM", logo = ""),
        score     = Score(home = 2, away = 1),
        league    = League(id = 61, name = "Ligue 1", country = "France", logo = "", flag = null)
    )

    /** Stub les 4 appels API nécessaires à [FootballRepository.getMatchDetail]. */
    private fun stubMatchDetailApiCalls(statusShort: String = "FT") {
        coEvery { api.getFixtureById(999) } returns FixturesResponseDto(listOf(buildFixtureResponseDto(statusShort)))
        coEvery { api.getFixtureStatistics(999) } returns StatisticsResponseDto(
            listOf(TeamStatisticsDto(team = teamDto, statistics = listOf(StatisticItemDto("Shots on Goal", "5"))))
        )
        coEvery { api.getFixtureEvents(999) } returns EventsResponseDto(
            listOf(MatchEventDto(
                time   = EventTimeDto(elapsed = 33, extra = null),
                team   = teamDto,
                player = EventPlayerDto(id = 7, name = "Mbappé"),
                type   = "Goal",
                detail = "Normal Goal"
            ))
        )
        coEvery { api.getFixtureLineups(999) } returns LineupsResponseDto(
            listOf(TeamLineupDto(
                team        = teamDto,
                formation   = "4-3-3",
                startXI     = listOf(LineupPlayerDto(LineupPlayerDetailDto(id = 1, name = "Donnarumma", number = 99, pos = "G"))),
                substitutes = emptyList()
            ))
        )
    }

    @Before
    fun setup() {
        api   = mockk()
        // relaxed = true : les méthodes non stubées retournent des valeurs par défaut (Unit, 0, etc.)
        cache = mockk(relaxed = true)
        repo  = FootballRepository(api, cache)
        // getInternal est générique : le mock relaxé ne garantit pas null pour T?.
        // On force explicitement null → simule un cache vide pour tous les tests.
        // Les tests "cache-hit" surchargent ce stub avec leurs propres données.
        @Suppress("UNCHECKED_CAST")
        coEvery { cache.getInternal(any(), any<Class<*>>()) } returns null
    }

    // ─── getTodayMatches ───────────────────────────────────────────────────────

    @Test
    fun `getTodayMatches - retourne les données du cache si disponible`() = runTest {
        val cachedMatches = listOf(buildMatchDomain())
        @Suppress("UNCHECKED_CAST")
        coEvery { cache.getInternal(eq("matches_today"), any<Class<*>>()) } returns (cachedMatches as Any?)

        val result = repo.getTodayMatches()

        assertTrue(result.isSuccess)
        assertEquals(cachedMatches, result.getOrNull())
        // L'API ne doit pas être appelée quand le cache est valide
        coVerify(exactly = 0) { api.getFixturesByDate(any(), any()) }
    }

    @Test
    fun `getTodayMatches - appelle l'API si le cache est vide`() = runTest {
        // relaxed = true → getInternal retourne null (cache vide), pas de stub nécessaire
        coEvery { api.getFixturesByDate(any(), any()) } returns
                FixturesResponseDto(response = listOf(buildFixtureResponseDto()))

        val result = repo.getTodayMatches()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(999, result.getOrNull()?.first()?.fixtureId)
    }

    @Test
    fun `getTodayMatches - met en cache les résultats avec TTL de 60 min`() = runTest {
        coEvery { api.getFixturesByDate(any(), any()) } returns
                FixturesResponseDto(response = listOf(buildFixtureResponseDto()))

        repo.getTodayMatches()

        coVerify { cache.set(eq("matches_today"), any<Any>(), eq(60)) }
    }

    @Test
    fun `getTodayMatches - retourne Result failure si l'API lève une exception`() = runTest {
        coEvery { api.getFixturesByDate(any(), any()) } throws RuntimeException("Erreur réseau")

        val result = repo.getTodayMatches()

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Erreur réseau", result.exceptionOrNull()?.message)
    }

    // ─── getMatchDetail ────────────────────────────────────────────────────────

    @Test
    fun `getMatchDetail - retourne les données du cache si disponible`() = runTest {
        val cached = MatchDetail(match = buildMatchDomain(), statistics = emptyList(), events = emptyList(), lineups = emptyList())
        @Suppress("UNCHECKED_CAST")
        coEvery { cache.getInternal(eq("match_detail_999"), any<Class<*>>()) } returns (cached as Any?)

        val result = repo.getMatchDetail(999)

        assertTrue(result.isSuccess)
        assertEquals(cached, result.getOrNull())
        coVerify(exactly = 0) { api.getFixtureById(any()) }
    }

    @Test
    fun `getMatchDetail - appelle les 4 endpoints en parallèle si cache vide`() = runTest {
        stubMatchDetailApiCalls()

        val result = repo.getMatchDetail(999)

        assertTrue(result.isSuccess)
        val detail = result.getOrNull()!!
        assertEquals(999, detail.match.fixtureId)
        assertEquals(1, detail.statistics.size)
        assertEquals(1, detail.events.size)
        assertEquals(1, detail.lineups.size)

        coVerify(exactly = 1) { api.getFixtureById(999) }
        coVerify(exactly = 1) { api.getFixtureStatistics(999) }
        coVerify(exactly = 1) { api.getFixtureEvents(999) }
        coVerify(exactly = 1) { api.getFixtureLineups(999) }
    }

    @Test
    fun `getMatchDetail - TTL adaptatif de 5 min pour un match LIVE`() = runTest {
        stubMatchDetailApiCalls(statusShort = "1H")
        repo.getMatchDetail(999)
        coVerify { cache.set(eq("match_detail_999"), any<Any>(), eq(5)) }
    }

    @Test
    fun `getMatchDetail - TTL adaptatif de 120 min pour un match FINISHED`() = runTest {
        stubMatchDetailApiCalls(statusShort = "FT")
        repo.getMatchDetail(999)
        coVerify { cache.set(eq("match_detail_999"), any<Any>(), eq(120)) }
    }

    @Test
    fun `getMatchDetail - TTL adaptatif de 120 min pour un match UPCOMING`() = runTest {
        stubMatchDetailApiCalls(statusShort = "NS")
        repo.getMatchDetail(999)
        coVerify { cache.set(eq("match_detail_999"), any<Any>(), eq(120)) }
    }

    @Test
    fun `getMatchDetail - retourne Result failure si une exception survient`() = runTest {
        coEvery { api.getFixtureById(any()) }      throws RuntimeException("Timeout")
        coEvery { api.getFixtureStatistics(any()) } throws RuntimeException("Timeout")
        coEvery { api.getFixtureEvents(any()) }     throws RuntimeException("Timeout")
        coEvery { api.getFixtureLineups(any()) }    throws RuntimeException("Timeout")

        val result = repo.getMatchDetail(999)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    // ─── getStandings ──────────────────────────────────────────────────────────

    private fun buildStandingsResponse() = StandingsResponseDto(
        response = listOf(
            StandingResponseDto(
                league = StandingLeagueDto(
                    id = 61, name = "Ligue 1",
                    standings = listOf(listOf(
                        StandingEntryDto(rank = 1, team = teamDto, points = 72, goalsDiff = 45,
                            all = StandingStatsDto(played = 30, win = 23, draw = 3, lose = 4)),
                        StandingEntryDto(rank = 2, team = TeamDto(id = 20, name = "OM", logo = ""), points = 65, goalsDiff = 20,
                            all = StandingStatsDto(played = 30, win = 20, draw = 5, lose = 5))
                    ))
                )
            )
        )
    )

    @Test
    fun `getStandings - retourne les données du cache si disponible`() = runTest {
        val cachedStandings = listOf(
            Standing(rank = 1, team = teamDomain, points = 72, played = 30, won = 23, drawn = 3, lost = 4, goalsDiff = 45)
        )
        @Suppress("UNCHECKED_CAST")
        coEvery { cache.getInternal(eq("standings_61"), any<Class<*>>()) } returns (cachedStandings as Any?)

        val result = repo.getStandings(61)

        assertTrue(result.isSuccess)
        assertEquals(cachedStandings, result.getOrNull())
        coVerify(exactly = 0) { api.getStandings(any(), any()) }
    }

    @Test
    fun `getStandings - appelle l'API et retourne le classement si cache vide`() = runTest {
        coEvery { api.getStandings(eq(61), any()) } returns buildStandingsResponse()

        val result = repo.getStandings(61)

        assertTrue(result.isSuccess)
        val standings = result.getOrNull()!!
        assertEquals(2, standings.size)
        assertEquals(1, standings[0].rank)
        assertEquals("PSG", standings[0].team.name)
    }

    @Test
    fun `getStandings - retourne liste vide si la réponse API n'a pas de classement`() = runTest {
        coEvery { api.getStandings(any(), any()) } returns StandingsResponseDto(response = emptyList())

        val result = repo.getStandings(61)

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Standing>(), result.getOrNull())
    }

    @Test
    fun `getStandings - met en cache avec TTL de 30 min`() = runTest {
        coEvery { api.getStandings(any(), any()) } returns buildStandingsResponse()
        repo.getStandings(61)
        coVerify { cache.set(eq("standings_61"), any<Any>(), eq(30)) }
    }

    @Test
    fun `getStandings - retourne Result failure si l'API lève une exception`() = runTest {
        coEvery { api.getStandings(any(), any()) } throws RuntimeException("Service indisponible")

        val result = repo.getStandings(61)

        assertFalse(result.isSuccess)
        assertEquals("Service indisponible", result.exceptionOrNull()?.message)
    }

    // ─── getTeams ──────────────────────────────────────────────────────────────

    private val teamsApiResponse = TeamsResponseDto(
        response = listOf(
            TeamEntryDto(team = TeamDto(id = 20, name = "Olympique de Marseille", logo = "https://om.logo")),
            TeamEntryDto(team = TeamDto(id = 85, name = "Paris Saint-Germain",    logo = "https://psg.logo")),
            TeamEntryDto(team = TeamDto(id = 93, name = "AS Monaco",              logo = "https://monaco.logo"))
        )
    )

    @Test
    fun `getTeams - retourne les données du cache si disponible`() = runTest {
        val cachedTeams = listOf(teamDomain)
        // En avril 2026, currentSeason() retourne 2025 (mois < 8 → saison de l'année précédente)
        @Suppress("UNCHECKED_CAST")
        coEvery { cache.getInternal(eq("teams_61_2025"), any<Class<*>>()) } returns (cachedTeams as Any?)

        val result = repo.getTeams(61)

        assertTrue(result.isSuccess)
        assertEquals(cachedTeams, result.getOrNull())
        coVerify(exactly = 0) { api.getTeamsByLeague(any(), any()) }
    }

    @Test
    fun `getTeams - appelle l'API si le cache est vide`() = runTest {
        coEvery { api.getTeamsByLeague(eq(61), any()) } returns teamsApiResponse

        val result = repo.getTeams(61)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `getTeams - retourne les équipes triées par nom alphabétique`() = runTest {
        coEvery { api.getTeamsByLeague(any(), any()) } returns teamsApiResponse

        val result = repo.getTeams(61)
        val teams  = result.getOrNull()!!

        // AS Monaco < Olympique de Marseille < Paris Saint-Germain
        assertEquals("AS Monaco",              teams[0].name)
        assertEquals("Olympique de Marseille", teams[1].name)
        assertEquals("Paris Saint-Germain",    teams[2].name)
    }

    @Test
    fun `getTeams - met en cache avec TTL de 1440 min (24h)`() = runTest {
        coEvery { api.getTeamsByLeague(any(), any()) } returns teamsApiResponse
        repo.getTeams(61)
        coVerify { cache.set(eq("teams_61_2025"), any<Any>(), eq(1440)) }
    }

    @Test
    fun `getTeams - retourne Result failure si l'API lève une exception`() = runTest {
        coEvery { api.getTeamsByLeague(any(), any()) } throws RuntimeException("Clé API invalide")

        val result = repo.getTeams(61)

        assertFalse(result.isSuccess)
        assertEquals("Clé API invalide", result.exceptionOrNull()?.message)
    }
}
