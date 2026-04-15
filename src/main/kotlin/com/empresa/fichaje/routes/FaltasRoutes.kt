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

        /*
        ========================
        REGISTRAR FALTA (ADMIN)
        ========================
        */

        post("/faltas/{userId}") {

            try {

                val principal = call.principal<JWTPrincipal>()!!

                val role = principal.payload
                    .getClaim("role")
                    .asString()

                if (role != "admin") {

                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val userId =
                    call.parameters["userId"]!!.toInt()

                val request =
                    call.receive<FaltaRequest>()

                service.registrar(
                    userId,
                    request.fecha,
                    request.tipo,
                    request.descripcion
                )

                call.respond(
                    mapOf("message" to "Falta registrada correctamente")
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to e.message)
                )
            }
        }


        /*
        ========================
        VER FALTAS (CON FILTROS)
        ========================
        */

        get("/faltas") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val userId =
                principal.payload
                    .getClaim("userId")
                    .asInt()

            val role =
                principal.payload
                    .getClaim("role")
                    .asString()


            val tipo =
                call.request.queryParameters["tipo"]

            val sortBy =
                call.request.queryParameters["sortBy"]

            val order =
                call.request.queryParameters["order"]


            val result =
                service.obtener(
                    userId,
                    role,
                    tipo,
                    sortBy,
                    order
                )

            call.respond(result)
        }


        /*
        ========================
        ELIMINAR FALTA (ADMIN)
        ========================
        */

        delete("/faltas/{id}") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val role =
                principal.payload
                    .getClaim("role")
                    .asString()

            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }

            val id =
                call.parameters["id"]!!.toInt()

            service.eliminar(id)

            call.respond(
                mapOf("message" to "Falta eliminada")
            )
        }
    }
}