package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class FaltaRequest(
    //val userId: Int,
    val fecha: String,
    val tipo: String,
    val descripcion: String
)