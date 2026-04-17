package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class VacacionesAlertaResponse(

    val userId: Int,
    val username: String,

    val diasNavidadUsados: Int,
    val diasNavidadPendientes: Int,

    val diasNavidadRestantes: Int,

    val urgente: Boolean
)