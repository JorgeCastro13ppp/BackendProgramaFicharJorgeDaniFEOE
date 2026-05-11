package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JornadaResponse(
    val id: Int,
    val userId: Int,          // 🔥 recomendable añadirlo también
    val username: String,
    val fecha: String,
    val entradaReal: Long?,
    val salidaReal: Long?,
    val tiempoLegal: Long,
    val tiempoTrabajoReal: Long,
    val tiempoExtraDetectado: Long,
    val cerradaAutomaticamente: Boolean,
    val corregidaPor: Int?,
    val comentarioAdmin: String?,
    val fechaCorreccion: Long?
)