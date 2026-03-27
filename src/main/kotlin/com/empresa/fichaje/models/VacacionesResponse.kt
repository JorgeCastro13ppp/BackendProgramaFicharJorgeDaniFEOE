package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class VacacionesResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val fechaInicio: String,
    val fechaFin: String,
    val estado: String
)