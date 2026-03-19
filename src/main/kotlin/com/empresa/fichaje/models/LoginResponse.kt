package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val message: String,
    val userId: Int
)
