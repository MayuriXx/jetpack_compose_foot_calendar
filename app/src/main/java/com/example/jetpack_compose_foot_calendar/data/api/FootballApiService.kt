package com.example.jetpack_compose_foot_calendar.data.api

import com.example.jetpack_compose_foot_calendar.data.api.dto.EventsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.FixturesResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.LineupsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StatisticsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the [API-Football](https://www.api-football.com/) REST API
 * (hosted at `v3.football.api-sports.io`).
 *
 * Authentication is handled globally by OkHttp via the `x-apisports-key` header interceptor
 * configured in [RetrofitClient]. All functions are `suspend` and intended to be called from
 * a coroutine scope.
 */
interface FootballApiService {

    /**
     * Retrieves all fixtures scheduled for a given date.
     *
     * Endpoint: `GET /fixtures`
     *
     * @param date     Date in ISO-8601 format (e.g. `"2024-04-21"`).
     * @param leagueId Optional league filter; pass `null` to retrieve all leagues.
     * @param season   The season year (defaults to `2024`).
     * @return A [FixturesResponseDto] wrapping the list of matching fixtures.
     */
    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String,
        @Query("league") leagueId: Int? = null,
        @Query("season") season: Int = 2024
    ): FixturesResponseDto

    /**
     * Retrieves a single fixture by its unique identifier.
     *
     * Endpoint: `GET /fixtures`
     *
     * @param fixtureId The API-Football fixture ID.
     * @return A [FixturesResponseDto] containing exactly one fixture in the response list.
     */
    @GET("fixtures")
    suspend fun getFixtureById(
        @Query("id") fixtureId: Int
    ): FixturesResponseDto

    /**
     * Retrieves per-team statistics for a given fixture.
     *
     * Endpoint: `GET /fixtures/statistics`
     *
     * @param fixtureId The API-Football fixture ID.
     * @return A [StatisticsResponseDto] containing statistics for both teams.
     */
    @GET("fixtures/statistics")
    suspend fun getFixtureStatistics(
        @Query("fixture") fixtureId: Int
    ): StatisticsResponseDto

    /**
     * Retrieves the timeline of events (goals, cards, substitutions, VAR) for a fixture.
     *
     * Endpoint: `GET /fixtures/events`
     *
     * @param fixtureId The API-Football fixture ID.
     * @return An [EventsResponseDto] containing the ordered list of match events.
     */
    @GET("fixtures/events")
    suspend fun getFixtureEvents(
        @Query("fixture") fixtureId: Int
    ): EventsResponseDto

    /**
     * Retrieves the starting line-ups and substitutes for both teams.
     *
     * Endpoint: `GET /fixtures/lineups`
     *
     * @param fixtureId The API-Football fixture ID.
     * @return A [LineupsResponseDto] containing the lineup for each team.
     */
    @GET("fixtures/lineups")
    suspend fun getFixtureLineups(
        @Query("fixture") fixtureId: Int
    ): LineupsResponseDto

    /**
     * Retrieves the league standings table for a given season.
     *
     * Endpoint: `GET /standings`
     *
     * @param leagueId The API-Football league ID.
     * @param season   The season year (defaults to `2024`).
     * @return A [StandingsResponseDto] containing the ranked list of teams.
     */
    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int = 2024
    ): StandingsResponseDto
}