package com.empresa.fichaje.routes

import com.empresa.fichaje.models.HorasDiaRequest
import com.empresa.fichaje.services.HorasService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.horasRoutes() {

    val service = HorasService()

    route("/horas-dia") {

        post {

            try {

                val request = call.receive<HorasDiaRequest>()

                val resultado = service.calcularHoras(
                    request.userId,
                    request.fechaInicio,
                    request.fechaFin
                )

                call.respond(resultado)

            } catch (e: Exception) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Error calculando horas"))
                )
            }
        }
    }
}