package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class EditarJornadaRequest(
    val nuevaSalidaReal: Long,
    val comentarioAdmin: String? = null
)