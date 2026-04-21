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

class FootballRepository(
    private val api: FootballApiService,
    private val cache: CacheManager
) {

    // ─── Matchs du jour ───────────────────────────────────────────

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

    // ─── Détail d'un match ────────────────────────────────────────

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

    // ─── Classement ───────────────────────────────────────────────

    suspend fun getStandings(leagueId: Int): Result<List<Standing>> {
        val cacheKey = "standings_$leagueId"

        val cached = cache.get<List<Standing>>(cacheKey)
        if (cached != null) return Result.success(cached)

        return try {
            val response = api.getStandings(leagueId)
            val standings = response.response
                .firstOrNull()
                ?.league
                ?.standings
                ?.firstOrNull()      // première poule (ligue normale = 1 seule poule)
                ?.map { it.toDomain() }
                ?: emptyList()

            cache.set(cacheKey, standings, ttlMinutes = 30)
            Result.success(standings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}