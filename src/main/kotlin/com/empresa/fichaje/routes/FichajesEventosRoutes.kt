package com.empresa.fichaje.routes

import com.empresa.fichaje.models.FichajeEventoRequest
import com.empresa.fichaje.models.FichajeEventoResponse
import com.empresa.fichaje.services.FichajesEventosService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.fichajesEventosRoutes() {

    val service = FichajesEventosService()

    authenticate("auth-jwt") {

        route("/fichajes-eventos") {

            post {

                try {

                    val request =
                        call.receive<FichajeEventoRequest>()

                    val id =
                        service.crearFichajeEvento(request)

                    call.respond(
                        HttpStatusCode.Created,
                        FichajeEventoResponse(id)
                    )

                } catch (e: Exception) {

                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf(
                            "error" to (
                                    e.message
                                        ?: "Error al registrar fichaje"
                                    )
                        )
                    )
                }
            }
        }


        get("/admin/fichajes") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val role =
                principal.payload
                    .getClaim("role")
                    .asString()

            if (role != "admin") {

                call.respond(
                    HttpStatusCode.Forbidden
                )

                return@get
            }

            val userIdParam =
                call.request.queryParameters["userId"]
                    ?.toIntOrNull()

            val fichajes =
                if (userIdParam != null) {

                    service
                        .obtenerFichajesPorUsuarioParaAdmin(
                            userIdParam
                        )

                } else {

                    service
                        .obtenerTodosParaAdmin()
                }

            call.respond(fichajes)
        }

        delete("/admin/fichajes/{id}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }

            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {

                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val service = FichajesEventosService()

            val eliminado = service.eliminarEvento(id)

            if (eliminado) {

                call.respond(HttpStatusCode.OK)

            } else {

                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/admin/fichajes-hoy") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val service = FichajesEventosService()

            val total = service.contarFichajesHoy()

            call.respond(mapOf("total" to total))
        }

        get("/admin/dashboard-fichajes-hoy") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val resumen =
                FichajesEventosService()
                    .resumenFichajesHoy()

            call.respond(resumen)
        }
    }
}