package com.empresa.fichaje.utils

import com.empresa.fichaje.domain.enums.EstadoVacaciones

fun String.toEstadoVacacionesOrNull(): EstadoVacaciones? =
    runCatching {
        EstadoVacaciones.valueOf(this.uppercase())
    }.getOrNull()