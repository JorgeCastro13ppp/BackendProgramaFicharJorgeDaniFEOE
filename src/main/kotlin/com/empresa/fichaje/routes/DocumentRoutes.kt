package com.empresa.fichaje.routes

import com.empresa.fichaje.models.DocumentRequest
import com.empresa.fichaje.services.DocumentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Route.documentRoutes() {

    val service = DocumentService()

    authenticate("auth-jwt") {

        /*
        ========================
        CREAR DOCUMENTO (ADMIN)
        ========================
        */

        post("/documentos") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val role =
                principal.payload
                    .getClaim("role")
                    .asString()


            if (role != "admin") {

                call.respond(
                    HttpStatusCode.Forbidden,
                    "No autorizado"
                )

                return@post
            }


            val request =
                call.receive<DocumentRequest>()

            service.createDocument(request)

            call.respond(
                mapOf("message" to "Documento creado")
            )
        }


        /*
        ========================
        DOCUMENTOS DEL USUARIO
        ========================
        */

        get("/documentos") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val userId =
                principal.payload
                    .getClaim("userId")
                    .asInt()


            val tipo =
                call.request
                    .queryParameters["tipo"]


            val sortBy =
                call.request
                    .queryParameters["sortBy"]

            val order =
                call.request
                    .queryParameters["order"]


            val docs =
                service.getDocuments(
                    userId = userId,
                    tipo = tipo,
                    sortBy = sortBy,
                    order = order
                )


            call.respond(docs)
        }


        /*
        ========================
        ELIMINAR DOCUMENTO
        ========================
        */

        delete("/admin/documentos/{id}") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val role =
                principal.payload
                    .getClaim("role")
                    .asString()


            if (role != "admin") {

                call.respond(
                    HttpStatusCode.Forbidden,
                    "No autorizado"
                )

                return@delete
            }


            val id =
                call.parameters["id"]
                    ?.toIntOrNull()


            if (id == null) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    "ID inválido"
                )

                return@delete
            }


            service.deleteDocument(id)


            call.respond(
                mapOf("message" to "Documento eliminado")
            )
        }


        /*
        ========================
        LISTADO ADMIN COMPLETO
        ========================
        */

        get("/admin/documentos") {

            val principal =
                call.principal<JWTPrincipal>()!!

            val role =
                principal.payload
                    .getClaim("role")
                    .asString()


            if (role != "admin") {

                call.respond(HttpStatusCode.Forbidden)

                return@get
            }


            val userId =
                call.request
                    .queryParameters["userId"]
                    ?.toIntOrNull()


            val tipo =
                call.request
                    .queryParameters["tipo"]


            val sortBy =
                call.request
                    .queryParameters["sortBy"]

            val order =
                call.request
                    .queryParameters["order"]


            val docs =
                service.getDocuments(
                    userId = userId,
                    tipo = tipo,
                    sortBy = sortBy,
                    order = order
                )


            call.respond(docs)
        }
    }
}