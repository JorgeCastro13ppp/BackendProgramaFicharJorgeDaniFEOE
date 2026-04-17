package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class FichajeResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val fechaHora: Long,
    val tipo: String,
    val latitud: Double?,
    val longitud: Double?,
    val accuracy: Double?
)