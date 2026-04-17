package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class VacacionesResumenAdminResponse(

    val userId: Int,
    val username: String,

    val diasNavidadRestantes: Int,
    val diasLibresRestantes: Int,

    val diasTotalesRestantes: Int
)