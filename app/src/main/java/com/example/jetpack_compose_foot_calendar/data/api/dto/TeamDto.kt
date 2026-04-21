package com.example.jetpack_compose_foot_calendar.data.api.dto

import com.example.jetpack_compose_foot_calendar.domain.model.Team
import com.google.gson.annotations.SerializedName

/** Envelope returned by `GET /teams`. */
data class TeamsResponseDto(
    @SerializedName("response") val response: List<TeamEntryDto>
)

/** One entry in the `/teams` response — wraps the team sub-object. */
data class TeamEntryDto(
    @SerializedName("team") val team: TeamInfoDto
)

/** Raw team fields from the API. */
data class TeamInfoDto(
    @SerializedName("id")   val id:   Int,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String
)

/** Maps the raw DTO to the domain [Team] model. */
fun TeamEntryDto.toDomain() = Team(
    id   = team.id,
    name = team.name,
    logo = team.logo
)

