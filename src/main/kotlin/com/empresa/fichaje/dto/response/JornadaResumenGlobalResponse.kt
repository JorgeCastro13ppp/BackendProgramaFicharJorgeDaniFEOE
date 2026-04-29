package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JornadaResumenGlobalResponse(

    val mes: String,

    val totalJornadas: Long,

    val jornadasAutomaticas: Long,

    val jornadasCorregidas: Long,

    val totalTiempoLegal: Long,

    val totalTiempoExtra: Long,

    val totalTrabajadores: Long
)