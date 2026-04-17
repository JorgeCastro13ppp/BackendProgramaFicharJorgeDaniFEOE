package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.VacacionesRequest
import com.empresa.fichaje.services.VacacionesService
import com.empresa.fichaje.utils.extractFilters
import com.empresa.fichaje.utils.requireAdmin
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.requireQueryParam
import com.empresa.fichaje.utils.role
import com.empresa.fichaje.utils.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.vacacionesRoutes() {

    val service = VacacionesService()

    authenticate("auth-jwt") {

        /*
        ========================
        CREAR VACACIONES (ADMIN)
        ========================
        */

        post("/admin/vacaciones/{userId}") {

            try {

                if (!call.requireAdmin()) return@post

                val userId =
                    call.parameters["userId"]?.toIntOrNull()

                if (userId == null) {

                    call.respond(HttpStatusCode.BadRequest)

                    return@post
                }

                val request =
                    call.receive<VacacionesRequest>()

                service.solicitar(
                    userId,
                    request.fechaInicio,
                    request.fechaFin
                )

                call.respond(
                    mapOf("message" to "Vacaciones creadas correctamente")
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
        SOLICITAR VACACIONES (USER)
        ========================
        */

        post("/vacaciones") {

            val principal =
                call.requirePrincipal()

            val request =
                call.receive<VacacionesRequest>()

            service.solicitar(
                principal.userId(),
                request.fechaInicio,
                request.fechaFin
            )

            call.respond(
                mapOf("message" to "Solicitud enviada")
            )
        }


        /*
        ========================
        VER VACACIONES (CON FILTROS)
        ========================
        */

        get("/vacaciones") {

            val principal =
                call.requirePrincipal()

            val filters =
                call.extractFilters(
                    principal,
                    allowUserOverride = false
                )

            val result =
                service.obtener(
                    filters.userId!!,
                    principal.role(),
                    filters.estado,
                    filters.sortBy,
                    filters.order
                )

            call.respond(result)
        }


        /*
        ========================
        APROBAR / RECHAZAR (ADMIN)
        ========================
        */

        put("/vacaciones/{id}") {

            if (!call.requireAdmin()) return@put

            val id =
                call.parameters["id"]!!.toInt()

            val estado =
                call.requireQueryParam("estado")

            service.actualizarEstado(
                id,
                estado
            )

            call.respond(
                mapOf("message" to "Estado actualizado")
            )
        }

        get("/vacaciones/resumen") {

            val principal =
                call.requirePrincipal()

            val resumen =
                service.obtenerResumenUsuario(
                    principal.userId()
                )

            call.respond(resumen)
        }

        get("/admin/vacaciones/resumen/{userId}") {

            if (!call.requireAdmin()) return@get

            val userId =
                call.parameters["userId"]?.toIntOrNull()

            if (userId == null) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "UserId inválido")
                )

                return@get
            }

            val resumen =
                service.obtenerResumenUsuario(userId)

            call.respond(resumen)
        }

        get("/admin/vacaciones/resumen") {

            if (!call.requireAdmin()) return@get

            val resumen =
                service.obtenerResumenTodosUsuarios()

            call.respond(resumen)
        }

        get("/admin/vacaciones/alertas") {

            if (!call.requireAdmin()) return@get

            val alertas =
                service.obtenerAlertasNavidad()

            call.respond(alertas)
        }

    }
}