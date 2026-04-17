package com.empresa.fichaje.utils

import com.empresa.fichaje.dto.request.HorasExtrasFilter
import io.ktor.server.application.*

fun ApplicationCall.horasExtrasFilter(): HorasExtrasFilter {

    val params = request.queryParameters

    return HorasExtrasFilter(

        estado =
            params["estado"],

        userId =
            params["userId"]?.toIntOrNull(),

        desde =
            params["desde"],

        hasta =
            params["hasta"]
    )
}