package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JornadaIncidenciaResponse(

    val jornadaId: Int,

    val userId: Int,

    val fecha: String,

    val tipo: String
)