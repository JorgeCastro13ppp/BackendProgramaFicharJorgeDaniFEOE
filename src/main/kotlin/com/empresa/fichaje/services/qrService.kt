package com.empresa.fichaje.services

import com.empresa.fichaje.models.QrToken
import java.util.*

class QrService {

    private var currentToken: QrToken? = null

    fun generateToken(): String {
        val token = UUID.randomUUID().toString()

        currentToken = QrToken(
            token = token,
            createdAt = System.currentTimeMillis()
        )

        return token
    }

    fun isValid(token: String): Boolean {

        val qr = currentToken ?: return false

        val now = System.currentTimeMillis()

        val validTime = 60_000 // 60 segundos

        return qr.token == token && (now - qr.createdAt) < validTime
    }
}