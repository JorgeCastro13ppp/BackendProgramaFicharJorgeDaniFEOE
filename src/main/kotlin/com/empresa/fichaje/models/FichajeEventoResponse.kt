package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class FichajeEventoResponse(
    val id: Int,
    val message: String = "Fichaje registrado correctamente"
)
