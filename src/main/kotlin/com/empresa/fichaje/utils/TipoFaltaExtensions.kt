package com.empresa.fichaje.utils

import com.empresa.fichaje.domain.enums.TipoFalta

fun String.toTipoFaltaOrNull(): TipoFalta? =
    runCatching {
        TipoFalta.valueOf(this.uppercase())
    }.getOrNull()