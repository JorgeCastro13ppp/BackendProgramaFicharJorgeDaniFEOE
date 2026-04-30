package com.empresa.fichaje.services

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

object PushService {

    fun sendFaltaNotification(
        token: String,
        motivo: String
    ) {

        val message = Message.builder()
            .setToken(token)
            .putData("title", "Nueva falta registrada")
            .putData("body", "Motivo: $motivo")
            .build()

        val response =
            FirebaseMessaging
                .getInstance()
                .send(message)

        println("PUSH RESPONSE: $response")
    }
}