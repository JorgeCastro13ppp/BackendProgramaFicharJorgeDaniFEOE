package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioResponse(
    val id: Int,
    val username: String,
    val role: String
)