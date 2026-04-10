package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class SiguientesAccionesResponse(

    val estado: String,

    val accionesPermitidas: List<String>
)