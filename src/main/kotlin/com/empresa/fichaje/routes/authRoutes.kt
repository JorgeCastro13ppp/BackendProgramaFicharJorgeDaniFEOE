package com.empresa.fichaje.routes


import com.empresa.fichaje.models.LoginRequest
import com.empresa.fichaje.models.LoginResponse
import com.empresa.fichaje.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {

    val authService = AuthService()

    post("/login") {

        val request = call.receive<LoginRequest>()

        val response = authService.login(request)

        if (response != null) {
            call.respond(response)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Credenciales incorrectas")
        }
    }
}