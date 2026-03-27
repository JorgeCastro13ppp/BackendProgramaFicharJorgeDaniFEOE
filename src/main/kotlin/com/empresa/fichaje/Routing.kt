package com.empresa.fichaje

import com.empresa.fichaje.routes.*
import com.empresa.fichaje.services.FichajeService
import com.empresa.fichaje.services.QrService
import io.ktor.server.application.*
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val qrService = QrService()

    val fichajeService = FichajeService(qrService)

    routing {

        authRoutes()

        fichajeRoutes(fichajeService)

        qrRoutes(qrService)

        documentRoutes()

        vacacionesRoutes()

        faltasRoutes()

        uploadRoutes()

        static("/files") {
            files("uploads")
        }
    }
}