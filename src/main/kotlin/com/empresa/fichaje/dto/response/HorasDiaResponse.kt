package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HorasDiaResponse(
    val tiempoTotal: Long,
    val tiempoTrabajo: Long,
    val tiempoViaje: Long,
    val tiempoDescanso: Long
)