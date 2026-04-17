package com.empresa.fichaje.utils

import com.empresa.fichaje.domain.enums.Role
import io.ktor.server.auth.jwt.JWTPrincipal

fun JWTPrincipal.role(): Role =
    Role.valueOf(payload.getClaim("role").asString())

fun JWTPrincipal.userId(): Int =
    payload.getClaim("userId").asInt()
fun JWTPrincipal.isAdmin(): Boolean =
    role() == Role.ADMIN