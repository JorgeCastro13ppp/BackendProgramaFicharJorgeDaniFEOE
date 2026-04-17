package com.empresa.fichaje.dto.response

import com.empresa.fichaje.domain.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val message: String,
    val token: String,
    val userId: Int,
    val role: Role
)
