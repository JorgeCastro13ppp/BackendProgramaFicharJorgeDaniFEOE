package com.empresa.fichaje.routes

import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.VacacionesRequest
import com.empresa.fichaje.services.VacacionesService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.vacacionesRoutes() {

    val service = VacacionesService()

    authenticate("auth-jwt") {

        post("/admin/vacaciones/{userId}") {

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

        // 👷 Solicitar vacaciones
        post("/vacaciones") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload
                .getClaim("userId")
                .asInt()

            val request = call.receive<VacacionesRequest>()

            service.solicitar(
                userId,
                request.fechaInicio,
                request.fechaFin
            )

            call.respond(mapOf("message" to "Solicitud enviada"))
        }


        // 📄 Ver vacaciones
        get("/vacaciones") {

            val estado =
                call.request.queryParameters["estado"]

            val sortBy =
                call.request.queryParameters["sortBy"]

            val order =
                call.request.queryParameters["order"]

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload
                .getClaim("userId")
                .asInt()

            val role = principal.payload
                .getClaim("role")
                .asString()

            val result = service.obtener(
                userId,
                role,
                estado,
                sortBy,
                order
            )

            call.respond(result)
        }


        // 👨‍💼 Aprobar / rechazar (solo admin)
        put("/vacaciones/{id}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }

            val id = call.parameters["id"]!!.toInt()

            val estado = call.request.queryParameters["estado"]!!

            service.actualizarEstado(id, estado)

            call.respond(mapOf("message" to "Estado actualizado"))
        }
    }
}