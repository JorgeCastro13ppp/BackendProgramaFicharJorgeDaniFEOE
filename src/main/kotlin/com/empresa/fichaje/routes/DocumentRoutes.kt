package com.empresa.fichaje.routes


import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.DocumentRequest
import com.empresa.fichaje.services.DocumentService

import io.ktor.http.*
import io.ktor.server.application.*   // IMPORTANTE (call)
import io.ktor.server.request.*       // receive
import io.ktor.server.response.*      // respond
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.documentRoutes() {

    val service = DocumentService()

    authenticate("auth-jwt") {

        post("/documentos") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload.getClaim("role").asString()

            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden, "No autorizado")
                return@post
            }

            val request = call.receive<DocumentRequest>()

            service.createDocument(request)

            call.respond(mapOf("message" to "Documento creado"))
        }


        get("/documentos") {

            val principal = call.principal<JWTPrincipal>()!!

            val userId = principal.payload.getClaim("userId").asInt()

            val docs = service.getDocuments(userId)

            call.respond(docs)
        }
    }
}