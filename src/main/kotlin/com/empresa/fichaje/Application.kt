package com.empresa.fichaje

import com.empresa.fichaje.database.DatabaseFactory
import com.empresa.fichaje.services.JwtService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    DatabaseFactory.init()

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {

        jwt("auth-jwt") {

            verifier(JwtService.verifier())

            validate { credential ->

                val userId =
                    credential.payload.getClaim("userId").asInt()

                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(CORS) {

        anyHost()

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowHeaders { true } // ← MUY IMPORTANTE

        allowCredentials = true
    }

    configureRouting()
}