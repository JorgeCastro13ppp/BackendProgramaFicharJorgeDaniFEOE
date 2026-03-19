package com.empresa.fichaje.services


import com.empresa.fichaje.models.FichajeRequest

class FichajeService(
    private val qrService: QrService
) {

    fun fichar(request: FichajeRequest): String {

        if (!qrService.isValid(request.token)) {
            return "Token inválido o expirado"
        }

        println("Usuario ${request.userId} fichó correctamente")

        return "Fichaje registrado correctamente"
    }
}