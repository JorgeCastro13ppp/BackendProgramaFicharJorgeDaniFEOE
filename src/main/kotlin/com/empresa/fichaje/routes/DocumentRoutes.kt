package com.empresa.fichaje.routes

import com.empresa.fichaje.dto.request.DocumentRequest
import com.empresa.fichaje.services.DocumentService
import com.empresa.fichaje.utils.extractFilters
import com.empresa.fichaje.utils.isAdmin
import io.ktor.http.*
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

            val principal = call.principal<JWTPrincipal>()!!

            if (!principal.isAdmin()) {

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

            val filters =
                call.extractFilters(
                    principal,
                    allowUserOverride = false
                )

            val docs =
                service.getDocuments(
                    userId = filters.userId,
                    tipo = filters.tipo,
                    sortBy = filters.sortBy,
                    order = filters.order
                )

            call.respond(docs)
        }


        /*
        ========================
        ELIMINAR DOCUMENTO (ADMIN)
        ========================
        */

        delete("/admin/documentos/{id}") {

            val principal =
                call.principal<JWTPrincipal>()!!

            if (!principal.isAdmin()) {

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

            if (!principal.isAdmin()) {

                call.respond(HttpStatusCode.Forbidden)

                return@get
            }

            val filters =
                call.extractFilters(
                    principal,
                    allowUserOverride = true
                )

            val docs =
                service.getDocuments(
                    userId = filters.userId,
                    tipo = filters.tipo,
                    sortBy = filters.sortBy,
                    order = filters.order
                )

            call.respond(docs)
        }
    }
}