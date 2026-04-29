package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JornadaPendienteRevisionResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val fecha: String,
    val entradaReal: Long?,
    val salidaReal: Long?,
    val cerradaAutomaticamente: Boolean,
    val tiempoExtraDetectado: Long
)