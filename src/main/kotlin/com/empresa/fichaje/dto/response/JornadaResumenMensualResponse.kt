package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JornadaResumenMensualResponse(

    val userId: Int,

    val mes: String,

    val totalJornadas: Long,

    val jornadasAutomaticas: Int,

    val jornadasCorregidas: Int,

    val totalTiempoLegal: Long,

    val totalTiempoExtra: Long
)