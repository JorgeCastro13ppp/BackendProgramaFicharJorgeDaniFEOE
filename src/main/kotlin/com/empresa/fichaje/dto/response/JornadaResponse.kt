package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JornadaResponse(
    val id: Int,
    val fecha: String,
    val entradaReal: Long?,
    val salidaReal: Long?,
    val tiempoLegal: Long,
    val tiempoExtraDetectado: Long,
    val cerradaAutomaticamente: Boolean,
    val corregidaPor: Int?,
    val comentarioAdmin: String?,
    val fechaCorreccion: Long?
)