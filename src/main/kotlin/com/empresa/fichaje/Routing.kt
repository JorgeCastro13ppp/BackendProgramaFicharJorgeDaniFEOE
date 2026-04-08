package com.empresa.fichaje

import com.empresa.fichaje.routes.*
import com.empresa.fichaje.services.QrService
import io.ktor.server.application.*
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.routing.*

fun Application.configureRouting() {



    routing {

        authRoutes()

        documentRoutes()

        vacacionesRoutes()

        faltasRoutes()

        uploadRoutes()

        fichajesEventosRoutes()

        horasRoutes()

        static("/files") {
            files("uploads")
        }
    }
}