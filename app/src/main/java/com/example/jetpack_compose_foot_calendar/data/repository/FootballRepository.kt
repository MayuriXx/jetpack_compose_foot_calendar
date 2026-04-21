package com.example.jetpack_compose_foot_calendar.data.repository

import com.example.jetpack_compose_foot_calendar.data.api.FootballApiService
import com.example.jetpack_compose_foot_calendar.data.api.dto.*
import com.example.jetpack_compose_foot_calendar.data.cache.CacheManager
import com.example.jetpack_compose_foot_calendar.domain.model.Match
import com.example.jetpack_compose_foot_calendar.domain.model.MatchDetail
import com.example.jetpack_compose_foot_calendar.domain.model.MatchStatus
import com.example.jetpack_compose_foot_calendar.domain.model.Standing
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Single source of truth for all football data in the application.
 *
 * Implements a **cache-first** strategy: every method checks [CacheManager] before making an
 * API call. Results are always wrapped in [Result] so callers can handle success and failure
 * without try/catch at the UI layer.
 *
 * @param api   The Retrofit service used to fetch data from API-Football.
 * @param cache The [CacheManager] used to persist and retrieve cached responses.
 */
class FootballRepository(
    private val api: FootballApiService,
    private val cache: CacheManager
) {

    /**
     * Returns the current football season year.
     *
     * Football seasons typically start in August/September:
     * - From August onwards → current year is the season (e.g. Aug 2025 → season 2025)
     * - Before August → previous year is the season (e.g. Apr 2026 → season 2025)
     */
    private fun currentSeason(): Int {
        val now = LocalDate.now()
        return if (now.monthValue >= 8) now.year else now.year - 1
    }

    // ─── Today's matches ──────────────────────────────────────────────────────

    /**
     * Returns today's fixtures for all leagues.
     *
     * Cache key: `"matches_today"` — TTL: **60 minutes** (today's matches can change status
     * throughout the day as games start and finish).
     *
     * @return [Result.success] with the list of today's [Match] objects, or
     *         [Result.failure] wrapping the exception if the API call fails.
     */
    suspend fun getTodayMatches(): Result<List<Match>> {
        val cacheKey = "matches_today"

        // 1. On vérifie le cache d'abord
        val cached = cache.get<List<Match>>(cacheKey)
        if (cached != null) {
            return Result.success(cached)
        }

        // 2. Sinon on appelle l'API
        return try {
            val today = LocalDate.now().toString() // "2024-01-15"
            val response = api.getFixturesByDate(date = today)
            val matches = response.response.map { it.toDomain() }

            // 3. On met en cache 60 min (matchs du jour peuvent changer)
            cache.set(cacheKey, matches, ttlMinutes = 60)

            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Match detail ─────────────────────────────────────────────────────────

    /**
     * Returns the full detail for a single fixture, including statistics, events and lineups.
     *
     * The four API calls (fixture, statistics, events, lineups) are executed in **parallel**
     * using [async]/[kotlinx.coroutines.Deferred.await] inside a [coroutineScope].
     *
     * The TTL is **adaptive** based on match status:
     * - [MatchStatus.LIVE] → 5 minutes (data changes rapidly)
     * - [MatchStatus.UPCOMING] or [MatchStatus.FINISHED] → 120 minutes
     * - Any other status → 60 minutes
     *
     * Cache key: `"match_detail_<fixtureId>"`
     *
     * @param fixtureId The unique fixture identifier.
     * @return [Result.success] with the [MatchDetail], or [Result.failure] on error.
     */
    suspend fun getMatchDetail(fixtureId: Int): Result<MatchDetail> {
        val cacheKey = "match_detail_$fixtureId"

        val cached = cache.get<MatchDetail>(cacheKey)
        if (cached != null) return Result.success(cached)

        return try {
            coroutineScope {
                // On fait les 4 appels en parallèle — équivalent de Promise.all()
                val matchDeferred = async { api.getFixtureById(fixtureId) }
                val statsDeferred = async { api.getFixtureStatistics(fixtureId) }
                val eventsDeferred = async { api.getFixtureEvents(fixtureId) }
                val lineupsDeferred = async { api.getFixtureLineups(fixtureId) }

                val match = matchDeferred.await().response.first().toDomain()
                val stats = statsDeferred.await().response.map { it.toDomain() }
                val events = eventsDeferred.await().response.map { it.toDomain() }
                val lineups = lineupsDeferred.await().response.map { it.toDomain() }

                val detail = MatchDetail(
                    match = match,
                    statistics = stats,
                    events = events,
                    lineups = lineups
                )

                // TTL adaptatif selon le statut du match — comme dans ton app Vue
                val ttl = when (match.status) {
                    MatchStatus.LIVE -> 5    // 5 min si en direct
                    MatchStatus.UPCOMING -> 120  // 2h si à venir
                    MatchStatus.FINISHED -> 120  // 2h si terminé
                    else -> 60
                }
                cache.set(cacheKey, detail, ttlMinutes = ttl)

                Result.success(detail)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Standings ────────────────────────────────────────────────────────────

    /**
     * Returns the league standings table for the given league.
     *
     * Only the first group of the first response entry is used, which corresponds to the main
     * table of regular league competitions. Cup competitions with multiple groups are not
     * fully supported.
     *
     * Cache key: `"standings_<leagueId>"` — TTL: **30 minutes**.
     *
     * @param leagueId The unique league identifier.
     * @return [Result.success] with the sorted list of [Standing] entries, or
     *         [Result.failure] on error. Returns an empty list if no standings are available.
     */
    suspend fun getStandings(leagueId: Int): Result<List<Standing>> {
        val cacheKey = "standings_$leagueId"

        val cached = cache.get<List<Standing>>(cacheKey)
        if (cached != null) return Result.success(cached)

        return try {
            val response = api.getStandings(leagueId, currentSeason())
            val standings = response.response
                .firstOrNull()
                ?.league
                ?.standings
                ?.firstOrNull()
                ?.map { it.toDomain() }
                ?: emptyList()

            cache.set(cacheKey, standings, ttlMinutes = 30)
            Result.success(standings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}