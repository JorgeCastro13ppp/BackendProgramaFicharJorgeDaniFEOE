package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class FichajeResponse(
    val id: Int,
    val userId: Int,
    val fechaHora: Long,
    val tipo: String
)