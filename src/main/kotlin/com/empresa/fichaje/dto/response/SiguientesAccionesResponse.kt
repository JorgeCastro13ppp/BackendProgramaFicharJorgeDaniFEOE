package com.empresa.fichaje.dto.response

import com.empresa.fichaje.domain.enums.AccionPermitida
import kotlinx.serialization.Serializable

@Serializable
data class SiguientesAccionesResponse(
    val estado: String,
    val accionesTaller: List<AccionPermitida>,

    val accionesObra: List<AccionPermitida>,

    val accionesReparacion: List<AccionPermitida>
)