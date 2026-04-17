package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class VacacionesRequest(
    val fechaInicio: String,
    val fechaFin: String
)