package com.empresa.fichaje.dto.request

data class HorasExtrasFilter(

    val estado: String?,

    val userId: Int?,

    val desde: String?,

    val hasta: String?
)