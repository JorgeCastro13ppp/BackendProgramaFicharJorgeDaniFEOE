package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class FichajeResponse(
    val userId: Int,
    val fechaHora: Long,
    val tipo: String
)