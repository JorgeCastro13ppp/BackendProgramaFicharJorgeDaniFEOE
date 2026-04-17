package com.empresa.fichaje.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ContextoFichaje {
    TALLER,
    OBRA,
    REPARACION
}