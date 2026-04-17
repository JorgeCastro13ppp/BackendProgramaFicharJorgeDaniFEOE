package com.empresa.fichaje.dto.request

import com.empresa.fichaje.domain.enums.AccionFichaje
import com.empresa.fichaje.domain.enums.ContextoFichaje
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