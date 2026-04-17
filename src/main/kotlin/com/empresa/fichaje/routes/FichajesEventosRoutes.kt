package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.FichajeEventoRequest
import com.empresa.fichaje.dto.response.FichajeEventoResponse
import com.empresa.fichaje.services.FichajesEventosService
import com.empresa.fichaje.utils.requireAdmin
import com.empresa.fichaje.utils.withIdParam
import com.empresa.fichaje.utils.withUserAccess
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.fichajesEventosRoutes() {

    val service = FichajesEventosService()

    authenticate("auth-jwt") {

        route("/fichajes-eventos") {

            /*
            ========================
            CREAR EVENTO
            ========================
            */

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


            /*
            ========================
            ÚLTIMO EVENTO
            ========================
            */

            get("/ultimo/{userId}") {

                call.withUserAccess { userId ->

                    val ultimoEvento =
                        service.obtenerUltimoEvento(userId)

                    if (ultimoEvento == null) {

                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf(
                                "error" to
                                        "No hay fichajes para este usuario"
                            )
                        )

                        return@withUserAccess
                    }

                    call.respond(ultimoEvento)
                }
            }


            /*
            ========================
            EVENTOS HOY
            ========================
            */

            get("/hoy/{userId}") {

                call.withUserAccess { userId ->

                    call.respond(
                        service.obtenerEventosHoy(userId)
                    )
                }
            }


            /*
            ========================
            ESTADO ACTUAL
            ========================
            */

            get("/estado/{userId}") {

                call.withUserAccess { userId ->

                    call.respond(
                        service.obtenerEstadoDetallado(userId)
                    )
                }
            }


            /*
            ========================
            SIGUIENTE ACCIÓN
            ========================
            */

            get("/siguiente-accion/{userId}") {

                call.withUserAccess { userId ->

                    call.respond(
                        service.obtenerAccionesPermitidas(userId)
                    )
                }
            }
        }


        /*
        ========================
        ADMIN: LISTADO FICHAJES
        ========================
        */

        get("/admin/fichajes") {

            if (!call.requireAdmin()) return@get

            val userIdParam =
                call.request.queryParameters["userId"]
                    ?.toIntOrNull()

            val sortBy =
                call.request.queryParameters["sortBy"]

            val order =
                call.request.queryParameters["order"]

            call.respond(
                service.obtenerFichajesParaAdmin(
                    userIdParam,
                    sortBy,
                    order
                )
            )
        }


        /*
        ========================
        ADMIN: ELIMINAR EVENTO
        ========================
        */

        delete("/admin/fichajes/{id}") {

            if (!call.requireAdmin()) return@delete

            call.withIdParam { id ->

                val eliminado =
                    service.eliminarEvento(id)

                call.respond(
                    if (eliminado)
                        HttpStatusCode.OK
                    else
                        HttpStatusCode.NotFound
                )
            }
        }


        /*
        ========================
        ADMIN: TOTAL HOY
        ========================
        */

        get("/admin/fichajes-hoy") {

            if (!call.requireAdmin()) return@get

            call.respond(
                mapOf(
                    "total" to service.contarFichajesHoy()
                )
            )
        }


        /*
        ========================
        ADMIN: DASHBOARD HOY
        ========================
        */

        get("/admin/dashboard-fichajes-hoy") {

            if (!call.requireAdmin()) return@get

            call.respond(
                service.resumenFichajesHoy()
            )
        }
    }
}