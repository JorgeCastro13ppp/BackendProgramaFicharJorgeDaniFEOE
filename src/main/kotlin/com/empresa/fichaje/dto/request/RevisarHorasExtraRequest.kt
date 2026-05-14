package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RevisarHorasExtraRequest(
    val estado: String,
    val comentario: String? = null
)