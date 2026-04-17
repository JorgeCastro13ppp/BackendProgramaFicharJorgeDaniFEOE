package com.empresa.fichaje.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DocumentResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val nombre: String,
    val tipo: String,
    val url: String
)