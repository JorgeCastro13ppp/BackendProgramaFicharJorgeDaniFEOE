package com.empresa.fichaje.utils

import com.empresa.fichaje.domain.enums.Role

fun String.toRole(): Role =
    Role.valueOf(this.uppercase())