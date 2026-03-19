package com.empresa.fichaje

import com.empresa.fichaje.routes.authRoutes
import com.empresa.fichaje.routes.fichajeRoutes
import com.empresa.fichaje.routes.qrRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        authRoutes()
        fichajeRoutes()
        qrRoutes()

    }
}
