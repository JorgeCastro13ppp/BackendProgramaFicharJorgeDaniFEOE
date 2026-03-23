package com.empresa.fichaje.routes

import com.empresa.fichaje.models.FaltaRequest
import com.empresa.fichaje.services.FaltasService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.faltasRoutes() {

    val service = FaltasService()

    authenticate("auth-jwt") {

        // 👨‍💼 Registrar falta a un trabajador (solo admin)
        post("/faltas/{userId}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden, "No autorizado")
                return@post
            }

            val userId = call.parameters["userId"]!!.toInt()

            val request = call.receive<FaltaRequest>()

            service.registrar(
                userId,
                request.fecha,
                request.tipo,
                request.descripcion
            )

            call.respond(mapOf("message" to "Falta registrada"))
        }


        // 📄 Ver faltas
        get("/faltas") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload
                .getClaim("userId")
                .asInt()

            val role = principal.payload
                .getClaim("role")
                .asString()

            val result = service.obtener(userId, role)

            call.respond(result)
        }


        // 🗑 Eliminar falta (solo admin)
        delete("/faltas/{id}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }

            val id = call.parameters["id"]!!.toInt()

            service.eliminar(id)

            call.respond(mapOf("message" to "Falta eliminada"))
        }
    }
}