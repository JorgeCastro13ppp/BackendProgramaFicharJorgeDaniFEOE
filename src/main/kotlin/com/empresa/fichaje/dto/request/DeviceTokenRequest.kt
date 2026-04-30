package com.empresa.fichaje.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequest(
    val token: String,
    val platform: String
)