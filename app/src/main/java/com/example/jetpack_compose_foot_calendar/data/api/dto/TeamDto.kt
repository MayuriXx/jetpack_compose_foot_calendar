package com.example.jetpack_compose_foot_calendar.data.api.dto

import com.google.gson.annotations.SerializedName

/** Envelope returned by `GET /teams`. */
data class TeamsResponseDto(
    @SerializedName("response") val response: List<TeamEntryDto>
)

/**
 * One entry in the `/teams` response.
 * The API wraps the team under a `"team"` key: `{ "team": { id, name, logo }, "venue": ... }`.
 * We reuse the existing [TeamDto] (from FixtureDto.kt) since the nested structure is identical.
 */
data class TeamEntryDto(
    @SerializedName("team") val team: TeamDto
)
