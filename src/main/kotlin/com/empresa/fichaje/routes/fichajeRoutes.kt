package com.empresa.fichaje.routes


import com.empresa.fichaje.database.FichajesTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.FichajeRequest
import com.empresa.fichaje.models.FichajeResponse
import com.empresa.fichaje.services.AppServices
import com.empresa.fichaje.services.FichajeService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Route.fichajeRoutes(fichajeService: FichajeService) {

    authenticate("auth-jwt") {

        post("/fichar") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload
                .getClaim("userId")
                .asInt()

            val request = call.receive<FichajeRequest>()

            try {

                fichajeService.registrarFichaje(
                    userId,
                    request.token,
                    request.tipo
                )

                call.respond(
                    mapOf("message" to "Fichaje registrado")
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    e.message ?: "QR inválido"
                )
            }
        }


        get("/fichajes") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload.getClaim("userId").asInt()

            val fichajes = fichajeService.obtenerFichajes(userId)

            call.respond(fichajes)
        }


        get("/horas") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload.getClaim("userId").asInt()

            val horas = fichajeService.calcularHoras(userId)

            call.respond(horas)
        }

        get("/horas/mensuales") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload
                .getClaim("userId")
                .asInt()

            val horas = fichajeService.horasMensuales(userId)

            call.respond(mapOf("horasMensuales" to horas))
        }
        get("/admin/horas/{userId}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId = call.parameters["userId"]!!.toInt()

            val horas = fichajeService.horasMensuales(userId)

            call.respond(mapOf("horasMensuales" to horas))
        }

        get("/admin/fichajes") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userIdParam = call.request.queryParameters["userId"]?.toIntOrNull()

            val fichajes = if (userIdParam != null) {
                fichajeService.obtenerFichajes(userIdParam)
            } else {
                fichajeService.obtenerTodos()
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

                call.respond(HttpStatusCode.BadRequest, "ID inválido")
                return@delete
            }

            fichajeService.eliminarFichaje(id)

            call.respond(mapOf("message" to "Fichaje eliminado"))
        }

        put("/admin/fichajes/{id}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden)
                return@put
            }

            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {

                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val request = call.receive<FichajeResponse>()

            fichajeService.actualizarFichaje(
                id,
                request.fechaHora,
                request.tipo
            )

            call.respond(mapOf("message" to "Fichaje actualizado"))
        }

        post("/admin/fichajes") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden)
                return@post
            }

            val request = call.receive<FichajeResponse>()

            fichajeService.crearFichajeManual(
                request.userId,
                request.fechaHora,
                request.tipo
            )

            call.respond(mapOf("message" to "Fichaje creado manualmente"))
        }
    }
}