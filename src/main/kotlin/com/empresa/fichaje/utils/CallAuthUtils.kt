package com.empresa.fichaje.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

fun ApplicationCall.requirePrincipal(): JWTPrincipal =
    principal<JWTPrincipal>()
        ?: error("JWTPrincipal requerido pero no encontrado")

suspend fun ApplicationCall.requireAdmin(): Boolean {

    val principal =
        requirePrincipal()

    if (!principal.isAdmin()) {

        respond(HttpStatusCode.Forbidden)

        return false
    }

    return true
}

fun ApplicationCall.requireQueryParam(name: String): String =
    request.queryParameters[name]
        ?: throw IllegalArgumentException("$name requerido")

suspend fun ApplicationCall.requireSelfOrAdmin(
    targetUserId: Int
): Boolean {

    val principal = requirePrincipal()

    if (
        principal.userId() != targetUserId &&
        !principal.isAdmin()
    ) {

        respond(HttpStatusCode.Forbidden)

        return false
    }

    return true
}

suspend inline fun ApplicationCall.withUserAccess(
    crossinline action: suspend (Int) -> Unit
) {

    val userIdParam =
        parameters["userId"]?.toIntOrNull()

    if (userIdParam == null) {

        respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "userId inválido")
        )

        return
    }

    if (!requireSelfOrAdmin(userIdParam))
        return

    action(userIdParam)
}

suspend inline fun ApplicationCall.withIdParam(
    crossinline action: suspend (Int) -> Unit
) {

    val id =
        parameters["id"]?.toIntOrNull()

    if (id == null) {

        respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "ID inválido")
        )

        return
    }

    action(id)
}