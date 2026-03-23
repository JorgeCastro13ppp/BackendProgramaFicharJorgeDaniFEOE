package com.empresa.fichaje

import com.empresa.fichaje.database.DatabaseFactory
import com.empresa.fichaje.services.JwtService
import com.empresa.fichaje.services.SecurityService
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    DatabaseFactory.init()

    install(ContentNegotiation) {
        json()
    }

    //install(io.ktor.server.plugins.multipart.Multipart)

    install(Authentication) {

        jwt("auth-jwt") {

            verifier(JwtService.verifier())

            validate { credential ->

                val userId = credential.payload.getClaim("userId").asInt()

                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    configureRouting()


}


