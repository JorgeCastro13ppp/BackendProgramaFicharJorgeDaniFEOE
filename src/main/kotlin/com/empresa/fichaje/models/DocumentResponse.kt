package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class DocumentResponse(
    val nombre: String,
    val tipo: String,
    val url: String
)