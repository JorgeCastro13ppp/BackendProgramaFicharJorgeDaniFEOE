package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.HorasDiaRequest
import com.empresa.fichaje.dto.request.HorasExtraEstadoRequest
import com.empresa.fichaje.services.HorasExtrasService
import com.empresa.fichaje.services.HorasService
import com.empresa.fichaje.utils.isAdmin
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.userId
import com.empresa.fichaje.utils.withUserAccess
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.horasRoutes() {

    val horasService = HorasService()
    val extrasService = HorasExtrasService()

    authenticate("auth-jwt") {

        /*
        ========================
        HORAS ENTRE FECHAS
        ========================
        */

        post("/horas-dia") {

            try {

                val principal =
                    call.requirePrincipal()

                val request =
                    call.receive<HorasDiaRequest>()

                if (!principal.isAdmin() &&
                    request.userId != principal.userId()
                ) {

                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val userId =
                    if (principal.isAdmin())
                        request.userId
                    else
                        principal.userId()

                val resultado =
                    horasService.calcularHoras(
                        userId,
                        request.fechaInicio,
                        request.fechaFin
                    )

                call.respond(resultado)

            } catch (e: Exception) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "error" to (
                                e.message
                                    ?: "Error calculando horas"
                                )
                    )
                )
            }
        }


        /*
        ========================
        HORAS HOY
        ========================
        */

        get("/horas/hoy/{userId}") {

            call.withUserAccess { userId ->

                call.respond(
                    horasService.resumenHorasHoy(userId)
                )
            }
        }


        /*
        ========================
        APROBAR / RECHAZAR EXTRA
        ========================
        */

        put("/horas-extra/{id}") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@put
            }

            val id =
                call.parameters["id"]!!.toInt()

            val request =
                call.receive<HorasExtraEstadoRequest>()

            extrasService.actualizarEstadoHorasExtra(
                id,
                request.estado,
                principal.userId(),
                request.comentario
            )

            call.respond(HttpStatusCode.OK)
        }


        /*
        ========================
        HORAS EXTRA DEL USUARIO
        ========================
        */

        get("/horas-extra/mias") {

            val principal =
                call.requirePrincipal()

            call.respond(
                extrasService.obtenerPorUsuario(
                    principal.userId()
                )
            )
        }


        /*
        ========================
        HORAS EXTRA PENDIENTES (ADMIN)
        ========================
        */

        get("/horas-extra/pendientes") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            call.respond(
                extrasService.obtenerPendientes()
            )
        }


        /*
        ========================
        BUSQUEDA FLEXIBLE (ADMIN)
        ========================
        */

        get("/horas-extra") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val estado =
                call.request.queryParameters["estado"]

            val userId =
                call.request.queryParameters["userId"]
                    ?.toIntOrNull()

            val desde =
                call.request.queryParameters["desde"]

            val hasta =
                call.request.queryParameters["hasta"]


            call.respond(
                extrasService.buscarHorasExtras(
                    estado,
                    userId,
                    desde,
                    hasta
                )
            )
        }


        /*
        ========================
        RESUMEN HORAS EXTRA (APP)
        ========================
        */

        get("/horas-extra/resumen-mias") {

            val principal =
                call.requirePrincipal()

            call.respond(
                extrasService.resumenUsuario(
                    principal.userId()
                )
            )
        }
    }
}