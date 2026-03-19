package com.empresa.fichaje.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*

import com.empresa.fichaje.services.AppServices
import io.ktor.http.ContentType

import io.ktor.http.*
import com.empresa.fichaje.services.QrGenerator

fun Route.qrRoutes() {

    val qrGenerator = QrGenerator()

    get("/qr") {

        val token = AppServices.qrService.generateToken()
        val imageBytes = qrGenerator.generateQrImage(token)

        call.respondBytes(
            imageBytes,
            ContentType.Image.PNG
        )
    }
}