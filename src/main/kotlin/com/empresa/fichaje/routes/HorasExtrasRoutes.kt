package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.HorasExtrasFilter
import com.empresa.fichaje.dto.request.RevisarHorasExtraRequest
import com.empresa.fichaje.services.HorasExtrasService
import com.empresa.fichaje.utils.isAdmin
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.horasExtrasRoutes() {

    val service = HorasExtrasService()

    authenticate("auth-jwt") {
        /*options("{...}") {
            call.respond(HttpStatusCode.OK)
        }*/
        /*
        ========================
        ADMIN - PENDIENTES
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
                service.obtenerPendientes()
            )
        }


        /*
        ========================
        USUARIO - MIS HORAS EXTRA
        ========================
        */

        get("/horas-extra/mias") {

            val principal =
                call.requirePrincipal()

            call.respond(
                service.obtenerPorUsuario(
                    principal.userId()
                )
            )
        }


        /*
        ========================
        ADMIN - BUSQUEDA FLEXIBLE
        ========================
        */

        get("/horas-extra") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val filter =
                HorasExtrasFilter(
                    estado = call.request.queryParameters["estado"],
                    userId = call.request.queryParameters["userId"]?.toIntOrNull(),
                    desde = call.request.queryParameters["desde"],
                    hasta = call.request.queryParameters["hasta"]
                )

            call.respond(
                service.buscarHorasExtras(filter)
            )
        }


        /*
        ========================
        RESUMEN USUARIO (APP)
        ========================
        */

        get("/horas-extra/resumen-mias") {

            val principal =
                call.requirePrincipal()

            call.respond(
                service.resumenUsuario(
                    principal.userId()
                )
            )
        }


        /*
        ========================
        RESUMEN EMPRESA (ADMIN)
        ========================
        */

        get("/horas-extra/resumen") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            call.respond(
                service.resumenEmpresa()
            )
        }

        put("/horas-extra/{id}") {

            val principal = call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)

                return@put
            }

            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf(
                            "error" to "ID inválido"
                        )
                    )

            val request =
                call.receive<RevisarHorasExtraRequest>()


            /*
            ========================
            VALIDAR ESTADO
            ========================
            */

            if (
                request.estado !in listOf(
                    "aprobado",
                    "rechazado"
                )
            ) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "error" to "Estado inválido"
                    )
                )

                return@put
            }


            /*
            ========================
            COMENTARIO OBLIGATORIO
            ========================
            */

            if (
                request.estado == "rechazado" &&
                request.comentario.isNullOrBlank()
            ) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "error" to
                                "Comentario obligatorio al rechazar"
                    )
                )

                return@put
            }


            /*
            ========================
            ACTUALIZAR
            ========================
            */

            service.actualizarEstadoHorasExtra(

                id = id,

                nuevoEstado = request.estado,

                adminId = principal.userId(),

                comentario = request.comentario
            )


            /*
            ========================
            RESPONSE
            ========================
            */

            call.respond(

                HttpStatusCode.OK,

                mapOf(
                    "message" to
                            "Horas extra actualizadas"
                )
            )
        }
    }
}