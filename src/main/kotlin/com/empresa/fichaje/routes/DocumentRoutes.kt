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

            val tipo = call.request.queryParameters["tipo"]

            val docs = service.getDocumentsFiltered(
                userId = userId,
                tipo = tipo
            )

            call.respond(docs)
        }
        delete("/admin/documentos/{id}") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden, "No autorizado")
                return@delete
            }

            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {

                call.respond(HttpStatusCode.BadRequest, "ID inválido")
                return@delete
            }

            service.deleteDocument(id)

            call.respond(mapOf("message" to "Documento eliminado"))
        }
        get("/admin/documentos") {

            val principal = call.principal<JWTPrincipal>()!!

            val role = principal.payload
                .getClaim("role")
                .asString()

            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userId = call.request
                .queryParameters["userId"]
                ?.toIntOrNull()

            val tipo = call.request
                .queryParameters["tipo"]

            val docs = service.getDocumentsFiltered(
                userId = userId,
                tipo = tipo
            )

            call.respond(docs)
        }
    }
}