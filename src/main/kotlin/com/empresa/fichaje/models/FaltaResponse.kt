package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class FaltaResponse(
    val id: Int,
    val userId: Int,
    val fecha: String,
    val tipo: String,
    val descripcion: String
)