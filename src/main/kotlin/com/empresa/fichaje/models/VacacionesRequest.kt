package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class VacacionesRequest(
    val fechaInicio: String,
    val fechaFin: String
)