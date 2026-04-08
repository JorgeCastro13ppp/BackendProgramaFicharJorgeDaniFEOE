package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class FichajeEventoRequest(
    val userId: Int,
    val timestamp: Long,
    val contexto: ContextoFichaje,
    val accion: AccionFichaje,
    val latitud: Double,
    val longitud: Double,
    val accuracy: Double
)