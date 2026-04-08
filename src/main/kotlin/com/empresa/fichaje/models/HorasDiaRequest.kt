package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class HorasDiaRequest(
    val userId: Int,
    val fechaInicio: Long,
    val fechaFin: Long
)