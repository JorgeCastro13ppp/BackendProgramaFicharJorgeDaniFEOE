package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class SiguientesAccionesResponse(
    val estado: String,
    val accionesTaller: List<String>,
    val accionesObra: List<String>,
    val accionesReparacion: List<String>
)