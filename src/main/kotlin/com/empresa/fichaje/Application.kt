package com.empresa.fichaje

import com.empresa.fichaje.database.DatabaseFactory
import com.empresa.fichaje.services.JwtService
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.respond
import io.ktor.server.routing.options
import io.ktor.server.routing.routing
import kotlinx.coroutines.launch

import kotlinx.coroutines.delay
import java.time.LocalTime
import com.empresa.fichaje.services.HorasService
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    dotenv()

    DatabaseFactory.init()

    install(ContentNegotiation) {
        json()
    }

    install(CORS) {

        anyHost()

       /*allowHost("localhost:3000")
        allowHost("127.0.0.1:3000")
        allowHost("192.168.1.45:3000")*/

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowCredentials = true
    }

    routing {
        options("{...}") {
            call.respond(HttpStatusCode.OK)
        }
    }

    install(Authentication) {

        jwt("auth-jwt") {

            verifier(JwtService.verifier)

            validate { credential ->

                val userId =
                    credential.payload
                        .getClaim("userId")
                        .asInt()

                if (userId != null)
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }

    configureRouting()

    environment.monitor.subscribe(
        ApplicationStarted
    ) {

        launch {

            while (true) {

                delay(60_000)

                val ahora =
                    LocalTime.now()

                if (
                    ahora.hour == 0 &&
                    ahora.minute == 0
                ) {

                    HorasService()
                        .cerrarJornadasAbiertasDelDiaAnterior()
                }
            }
        }
    }

    routing {

        static("/uploads") {

            files("uploads")
        }
    }
}