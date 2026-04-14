package com.empresa.fichaje.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.content.*
import java.io.File

fun Route.uploadRoutes() {

    post("/upload") {

        val multipart = call.receiveMultipart()

        val userId =
            call.request.queryParameters["userId"] ?: "general"

        var fileName: String? = null
        var error = false


        multipart.forEachPart { part ->

            if (part is PartData.FileItem) {

                val originalFileName =
                    part.originalFileName ?: "archivo.pdf"

                val uploadDir =
                    File("uploads/documentos/$userId")

                if (!uploadDir.exists()) {
                    uploadDir.mkdirs()
                }

                val uniqueFileName =
                    "${System.currentTimeMillis()}_$originalFileName"

                val fileBytes =
                    part.streamProvider().readBytes()

                val file =
                    File(uploadDir, uniqueFileName)

                file.writeBytes(fileBytes)

                fileName = uniqueFileName
            }

            part.dispose()
        }


        if (error) {

            call.respond(
                HttpStatusCode.BadRequest,
                "Nombre de archivo inválido"
            )

            return@post
        }


        if (fileName == null) {

            call.respond(
                HttpStatusCode.BadRequest,
                "Archivo no recibido"
            )

            return@post
        }


        call.respond(

            mapOf(
                "url" to "documentos/$userId/$fileName"
            )
        )
    }
}