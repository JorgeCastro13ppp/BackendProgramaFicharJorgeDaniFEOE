package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
enum class ContextoFichaje {
    TALLER,
    OBRA,
    REPARACION
}

@Serializable
enum class AccionFichaje {
    ENTRADA,
    SALIDA,
    INICIO_VIAJE,
    FIN_VIAJE,
    INICIO_DESCANSO,
    FIN_DESCANSO
}