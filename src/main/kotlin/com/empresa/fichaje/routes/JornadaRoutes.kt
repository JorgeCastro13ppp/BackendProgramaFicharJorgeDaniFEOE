package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.CorregirJornadaRequest
import com.empresa.fichaje.dto.request.EditarJornadaRequest
import com.empresa.fichaje.services.HorasService
import com.empresa.fichaje.utils.isAdmin
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.userId
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

fun Route.jornadasRoutes() {

    val service = HorasService()

    authenticate("auth-jwt") {

        put("/jornadas/{id}") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@put
            }


            val jornadaId =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest
                    )


            val request =
                call.receive<EditarJornadaRequest>()


            service.corregirSalidaJornada(
                jornadaId,
                request.nuevaSalidaReal,
                principal.userId(),
                request.comentarioAdmin
            )


            call.respond(HttpStatusCode.OK)
        }

        get("/jornadas/cerradas-automaticamente") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val jornadas =
                service.obtenerJornadasCerradasAutomaticamente()

            call.respond(jornadas)
        }

        get("/jornadas") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId =
                call.request.queryParameters["userId"]
                    ?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest
                    )

            val desde =
                call.request.queryParameters["desde"]

            val hasta =
                call.request.queryParameters["hasta"]


            val jornadas =
                service.obtenerJornadasPorUsuario(
                    userId,
                    desde,
                    hasta
                )

            call.respond(jornadas)
        }

        get("/jornadas/resumen") {

            val principal = call.requirePrincipal()

            if (!principal.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId =
                call.request.queryParameters["userId"]?.toIntOrNull()

            val mes =
                call.request.queryParameters["mes"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

            val resumen =
                if (userId != null) {
                    service.obtenerResumenMensual(userId, mes)
                } else {
                    service.obtenerResumenGlobalMensual(mes)
                }

            call.respond(resumen)
        }

        get("/jornadas/resumen-pendientes") {

            val principal = call.requirePrincipal()

            if (!principal.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId =
                call.request.queryParameters["userId"]?.toIntOrNull()

            val resumen =
                service.obtenerResumenPendientes(userId)

            call.respond(resumen)
        }

        get("/jornadas/pendientes-revision") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val jornadas =
                service.obtenerJornadasPendientesRevision()

            call.respond(jornadas)
        }

        put("/jornadas/corregir") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@put
            }


            val request =
                call.receive<CorregirJornadaRequest>()


            service.corregirJornada(

                jornadaId =
                    request.jornadaId,

                nuevaSalidaReal =
                    request.nuevaSalidaReal,

                adminId =
                    principal.userId(),

                comentario =
                    request.comentario
            )


            call.respond(HttpStatusCode.OK)
        }

        get("/jornadas/incidencias") {

            val principal =
                call.requirePrincipal()

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }


            val incidencias =
                service.obtenerIncidencias()


            call.respond(incidencias)
        }

        get("/admin/jornadas") {

            val principal = call.principal<JWTPrincipal>()!!

            if (!principal.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId =
                call.request.queryParameters["userId"]?.toIntOrNull()

            println("USER ID RECIBIDO: $userId")

            val jornadas =
                if (userId != null) {
                    service.obtenerJornadasPorUsuario(userId)
                } else {
                    service.obtenerTodasLasJornadas()
                }

            call.respond(jornadas)
        }

        post("/admin/jornadas/cerrar-abiertas") {

            val principal = call.requirePrincipal()

            if (!principal.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }

            service.cerrarJornadasAbiertasDelDiaAnterior()

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Jornadas abiertas cerradas correctamente")
            )
        }

        post("/admin/jornadas/cerrar") {

            val principal = call.requirePrincipal()

            if (!principal.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }

            val fecha =
                call.request.queryParameters["fecha"]
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Falta parámetro fecha"
                    )

            service.cerrarJornadasPorFecha(fecha)

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Jornadas cerradas para $fecha")
            )
        }

        get("/admin/jornadas/usuario/{id}") {

            val principal = call.requirePrincipal()

            if (!principal.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

            val jornadas =
                service.obtenerJornadasPorUsuario(userId)

            call.respond(jornadas)
        }
    }
}