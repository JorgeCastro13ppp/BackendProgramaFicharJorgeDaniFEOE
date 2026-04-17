package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.LoginRequest
import com.empresa.fichaje.dto.response.LoginResponse
import com.empresa.fichaje.services.AuthService
import com.empresa.fichaje.services.JwtService
import com.empresa.fichaje.utils.isAdmin
import com.empresa.fichaje.utils.userId
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {

    val authService = AuthService()

    /*
    ========================
    LOGIN
    ========================
    */

    post("/login") {

        val request = call.receive<LoginRequest>()

        val user = authService.login(request)

        if (user != null) {

            val token =
                JwtService.generateToken(
                    user.id,
                    user.role
                )

            call.respond(
                LoginResponse(
                    message = "Login correcto",
                    token = token,
                    userId = user.id,
                    role = user.role
                )
            )

        } else {

            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Credenciales incorrectas")
            )
        }
    }

    authenticate("auth-jwt") {

        /*
        ========================
        REGISTER (ADMIN)
        ========================
        */

        post("/register") {

            val principal = call.principal<JWTPrincipal>()!!

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden, "No autorizado")

                return@post
            }

            val request = call.receive<LoginRequest>()

            authService.register(request.username, request.password)

            call.respond(mapOf("message" to "Usuario creado"))
        }


        /*
        ========================
        LISTADO USUARIOS (ADMIN)
        ========================
        */

        get("/admin/usuarios") {

            val principal = call.principal<JWTPrincipal>()!!

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)

                return@get
            }

            val roleFilter =
                call.request.queryParameters["role"]

            val sortBy =
                call.request.queryParameters["sortBy"]

            val order =
                call.request.queryParameters["order"]

            val usuarios =
                authService.obtenerUsuarios(
                    roleFilter,
                    sortBy,
                    order
                )

            call.respond(usuarios)
        }


        /*
        ========================
        ELIMINAR USUARIO (ADMIN)
        ========================
        */

        delete("/admin/usuarios/{id}") {

            val principal = call.principal<JWTPrincipal>()!!

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)

                return@delete
            }

            val id =
                call.parameters["id"]?.toIntOrNull()

            if (id == null) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    "ID inválido"
                )

                return@delete
            }

            // 🚫 Evitar auto-eliminación admin

            if (principal.userId() == id) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    "No puedes eliminar tu propio usuario"
                )

                return@delete
            }

            val eliminado =
                authService.eliminarUsuario(id)

            if (eliminado) {

                call.respond(HttpStatusCode.OK)

            } else {

                call.respond(
                    HttpStatusCode.NotFound,
                    "Usuario no encontrado"
                )
            }
        }
    }
}