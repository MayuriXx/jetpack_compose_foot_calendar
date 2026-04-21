package com.example.jetpack_compose_foot_calendar.data.api

import com.example.jetpack_compose_foot_calendar.data.api.dto.EventsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.FixturesResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.LineupsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StandingsResponseDto
import com.example.jetpack_compose_foot_calendar.data.api.dto.StatisticsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String,
        @Query("league") leagueId: Int? = null,
        @Query("season") season: Int = 2024
    ): FixturesResponseDto

    @GET("fixtures")
    suspend fun getFixtureById(
        @Query("id") fixtureId: Int
    ): FixturesResponseDto

    @GET("fixtures/statistics")
    suspend fun getFixtureStatistics(
        @Query("fixture") fixtureId: Int
    ): StatisticsResponseDto

    @GET("fixtures/events")
    suspend fun getFixtureEvents(
        @Query("fixture") fixtureId: Int
    ): EventsResponseDto

    @GET("fixtures/lineups")
    suspend fun getFixtureLineups(
        @Query("fixture") fixtureId: Int
    ): LineupsResponseDto

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int = 2024
    ): StandingsResponseDto
}