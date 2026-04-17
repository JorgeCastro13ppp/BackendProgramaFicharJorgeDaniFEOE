package com.empresa.fichaje

import com.empresa.fichaje.routes.*
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureRouting() {

    routing {

        authRoutes()

        documentRoutes()

        vacacionesRoutes()

        faltasRoutes()

        uploadRoutes()

        fichajesEventosRoutes()

        horasRoutes()
        horasExtrasRoutes()

        staticFiles(
            remotePath = "/files",
            dir = File("uploads")
        )
    }
}