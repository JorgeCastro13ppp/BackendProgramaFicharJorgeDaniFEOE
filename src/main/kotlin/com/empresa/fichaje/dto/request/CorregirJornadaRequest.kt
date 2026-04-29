package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CorregirJornadaRequest(

    val jornadaId: Int,

    val nuevaSalidaReal: Long,

    val comentario: String? = null
)