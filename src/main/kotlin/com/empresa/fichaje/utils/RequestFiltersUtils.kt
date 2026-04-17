package com.empresa.fichaje.utils

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*

data class QueryFilters(
    val userId: Int?,
    val tipo: String?,
    val estado: String?,
    val sortBy: String?,
    val order: String?
)

fun ApplicationCall.extractFilters(
    principal: JWTPrincipal,
    allowUserOverride: Boolean = false
): QueryFilters {

    val userId =
        if (allowUserOverride)
            request.queryParameters["userId"]?.toIntOrNull()
        else
            principal.userId()

    return QueryFilters(
        userId = userId,
        tipo = request.queryParameters["tipo"],
        estado = request.queryParameters["estado"],
        sortBy = request.queryParameters["sortBy"],
        order = request.queryParameters["order"]
    )
}