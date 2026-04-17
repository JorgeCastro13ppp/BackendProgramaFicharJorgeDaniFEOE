package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class FichajeEventoResponse(
    val id: Int,
    val message: String = "Fichaje registrado correctamente"
)
