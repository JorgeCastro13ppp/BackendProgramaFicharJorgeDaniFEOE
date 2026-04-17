package com.empresa.fichaje.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class AccionFichaje {
    ENTRADA,
    SALIDA,
    INICIO_VIAJE,
    FIN_VIAJE,
    INICIO_DESCANSO,
    FIN_DESCANSO
}