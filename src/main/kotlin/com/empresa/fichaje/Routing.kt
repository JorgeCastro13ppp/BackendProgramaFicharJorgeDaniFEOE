package com.empresa.fichaje

import com.empresa.fichaje.routes.authRoutes
import com.empresa.fichaje.routes.documentRoutes
import com.empresa.fichaje.routes.faltasRoutes
import com.empresa.fichaje.routes.fichajeRoutes
import com.empresa.fichaje.routes.qrRoutes
import com.empresa.fichaje.routes.uploadRoutes
import com.empresa.fichaje.routes.vacacionesRoutes
import com.empresa.fichaje.services.FichajeService
import io.ktor.server.application.*
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        authRoutes()
        fichajeRoutes(FichajeService())
        qrRoutes()
        documentRoutes()
        vacacionesRoutes()
        faltasRoutes()
        uploadRoutes()

        static("/files") {
            files("uploads")
        }
    }

}
