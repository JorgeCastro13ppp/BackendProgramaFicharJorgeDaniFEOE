package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class HorasResponse(
    val fecha: String,
    val horasTrabajadas: Double
)