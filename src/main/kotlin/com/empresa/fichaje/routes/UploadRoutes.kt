package com.empresa.fichaje.routes

import com.empresa.fichaje.utils.isAdmin
import com.empresa.fichaje.utils.requirePrincipal
import com.empresa.fichaje.utils.userId
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.content.*
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File

fun Route.uploadRoutes() {

    authenticate("auth-jwt") {

        post("/upload") {

            val principal =
                call.requirePrincipal()

            val requestedUserId =
                call.request.queryParameters["userId"]?.toIntOrNull()

            val userId =
                if (requestedUserId != null && principal.isAdmin())
                    requestedUserId.toString()
                else
                    principal.userId().toString()

            val multipart =
                call.receiveMultipart()

            var fileName: String? = null
            var invalidExtension = false

            val allowedExtensions =
                setOf("pdf", "jpg", "jpeg", "png")

            multipart.forEachPart { part ->

                if (part is PartData.FileItem) {

                    val originalFileName =
                        part.originalFileName ?: "archivo.pdf"

                    val extension =
                        originalFileName.substringAfterLast('.', "")

                    if (extension.lowercase() !in allowedExtensions) {

                        invalidExtension = true
                        part.dispose()
                        return@forEachPart
                    }

                    val safeFileName =
                        originalFileName.replace(
                            Regex("[^a-zA-Z0-9._-]"),
                            "_"
                        )

                    val uploadDir =
                        File("uploads/documentos/$userId")

                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs()
                    }

                    val uniqueFileName =
                        "${System.currentTimeMillis()}_$safeFileName"

                    val file =
                        File(uploadDir, uniqueFileName)

                    // Escritura correcta con ByteReadChannel
                    part.provider().copyTo(file.outputStream())

                    fileName = uniqueFileName
                }

                part.dispose()
            }

            if (invalidExtension) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    "Tipo de archivo no permitido"
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
}