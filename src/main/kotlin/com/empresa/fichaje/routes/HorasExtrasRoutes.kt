package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.HorasExtrasFilter
import com.empresa.fichaje.services.HorasExtrasService
import com.empresa.fichaje.utils.isAdmin
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.horasExtrasRoutes() {

    val service =
        HorasExtrasService()

    authenticate("auth-jwt") {

        /*
        ========================
        ADMIN - PENDIENTES
        ========================
        */

        get("/horas-extra/pendientes") {

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
    }
}