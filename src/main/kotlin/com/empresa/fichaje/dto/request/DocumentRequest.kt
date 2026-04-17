package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class DocumentRequest(
    val userId: Int,
    val nombre: String,
    val tipo: String,
    val url: String
)