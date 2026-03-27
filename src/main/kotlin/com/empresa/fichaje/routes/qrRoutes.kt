package com.empresa.fichaje.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*

import com.empresa.fichaje.services.AppServices
import com.empresa.fichaje.services.AppServices.qrService
import io.ktor.http.ContentType

import io.ktor.http.*
import com.empresa.fichaje.services.QrGenerator
import com.empresa.fichaje.services.QrService
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun Route.qrRoutes(
    qrService: QrService
) {

    val qrGenerator = QrGenerator()


    get("/admin/qr") {

        val token = qrService.generateToken()

        val qrImage = qrGenerator.generateQrImage(token)

        call.respondBytes(
            qrImage,
            ContentType.Image.PNG
        )
    }
}