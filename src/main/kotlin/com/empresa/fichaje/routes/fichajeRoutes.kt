package com.empresa.fichaje.routes


import com.empresa.fichaje.models.FichajeRequest
import com.empresa.fichaje.services.AppServices
import com.empresa.fichaje.services.FichajeService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.fichajeRoutes() {

    val fichajeService = FichajeService(AppServices.qrService)

    post("/fichar") {

        val request = call.receive<FichajeRequest>()

        val response = fichajeService.fichar(request)

        call.respond(mapOf("message" to response))
    }
}