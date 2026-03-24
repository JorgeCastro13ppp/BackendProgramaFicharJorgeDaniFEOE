package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class DocumentResponse(
    val id: Int,
    val userId: Int,
    val nombre: String,
    val tipo: String,
    val url: String
)