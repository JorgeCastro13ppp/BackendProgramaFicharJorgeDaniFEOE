package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val message: String,
    val token: String,
    val userId: Int,
    val role: String
)
