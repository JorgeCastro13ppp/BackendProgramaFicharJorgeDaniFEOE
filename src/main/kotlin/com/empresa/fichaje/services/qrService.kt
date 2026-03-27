package com.empresa.fichaje.services

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

class QrService {

    private val secret = "qr-super-secret-key"

    private val validDuration = 60_000L // 60 segundos


    fun generateToken(): String {

        val timestamp = System.currentTimeMillis()

        val signature = sign(timestamp.toString())

        return "empresa:fichaje:$timestamp:$signature"
    }


    fun isValid(token: String): Boolean {

        val parts = token.split(":")

        if (parts.size != 4) return false

        val prefix = parts[0]
        val type = parts[1]
        val timestamp = parts[2]
        val signature = parts[3]

        if (prefix != "empresa" || type != "fichaje")
            return false

        val expectedSignature = sign(timestamp)

        if (signature != expectedSignature)
            return false

        val now = System.currentTimeMillis()

        return now - timestamp.toLong() <= validDuration
    }


    private fun sign(data: String): String {

        val mac = Mac.getInstance("HmacSHA256")

        val key = SecretKeySpec(
            secret.toByteArray(),
            "HmacSHA256"
        )

        mac.init(key)

        val raw = mac.doFinal(data.toByteArray())

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(raw)
    }
}