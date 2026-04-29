package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HorasExtrasResumenResponse(

    val pendientes: Int,
    val aprobadas: Int,
    val rechazadas: Int,
    val totalMinutos: Int
)