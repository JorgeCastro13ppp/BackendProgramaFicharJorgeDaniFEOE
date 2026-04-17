package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class FaltaResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val fecha: String,
    val tipo: String,
    val descripcion: String
)