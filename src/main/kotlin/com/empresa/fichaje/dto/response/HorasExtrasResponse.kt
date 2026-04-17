package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HorasExtrasResponse(

    val id: Int,

    val userId: Int,

    val fecha: String,

    val minutosExtra: Long,

    val estado: String,

    val aprobadoPor: Int?,

    val fechaRevision: Long?,

    val comentario: String?
)