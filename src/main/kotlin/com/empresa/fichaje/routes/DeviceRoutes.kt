package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.DeviceTokenRequest
import com.empresa.fichaje.services.DeviceTokenService
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.deviceRoutes() {

    val deviceTokenService = DeviceTokenService()

    authenticate("auth-jwt") {

        post("/device/register") {

            println("DEVICE REGISTER LLAMADO")

            val userId =
                call.requirePrincipal().userId()

            val request =
                call.receive<DeviceTokenRequest>()

            println("TOKEN: ${request.token}")

            deviceTokenService.save(
                userId,
                request.token,
                request.platform
            )

            call.respond(HttpStatusCode.OK)
        }
    }
}