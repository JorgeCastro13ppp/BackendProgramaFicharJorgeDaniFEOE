package com.empresa.fichaje.domain.models

import com.empresa.fichaje.domain.enums.Role

data class User(
    val id: Int,
    val username: String,
    val role: Role
)