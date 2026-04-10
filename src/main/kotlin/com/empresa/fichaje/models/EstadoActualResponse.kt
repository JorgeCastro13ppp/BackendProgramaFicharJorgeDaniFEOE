package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class EstadoActualResponse(

    val estado: String,

    val contexto: String?,

    val accion: String?,

    val timestamp: Long?
)