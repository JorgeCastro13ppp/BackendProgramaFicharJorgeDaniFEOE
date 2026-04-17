package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class VacacionesResumenResponse(

    val anio: Int,

    val diasNavidadUsados: Int,
    val diasNavidadRestantes: Int,

    val diasLibresUsados: Int,
    val diasLibresRestantes: Int,

    val diasTotalesRestantes: Int
)