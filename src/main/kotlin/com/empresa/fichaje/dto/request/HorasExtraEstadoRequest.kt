package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class HorasExtraEstadoRequest(
    val estado: String,
    val comentario: String? = null
)