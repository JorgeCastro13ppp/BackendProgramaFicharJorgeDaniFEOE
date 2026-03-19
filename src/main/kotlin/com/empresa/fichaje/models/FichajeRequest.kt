package com.empresa.fichaje.models

import kotlinx.serialization.Serializable

@Serializable
data class FichajeRequest(
    val userId: Int,
    val token: String

)