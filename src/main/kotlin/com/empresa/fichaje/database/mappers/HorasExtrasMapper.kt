package com.empresa.fichaje.database.mappers

import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.dto.response.HorasExtrasResponse
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toHorasExtrasResponse(): HorasExtrasResponse {

    return HorasExtrasResponse(

        id =
            this[HorasExtrasTable.id],

        userId =
            this[HorasExtrasTable.userId],

        fecha =
            this[HorasExtrasTable.fecha],

        minutosExtra =
            this[HorasExtrasTable.minutosExtra],

        estado =
            this[HorasExtrasTable.estado],

        aprobadoPor =
            this[HorasExtrasTable.aprobadoPor],

        fechaRevision =
            this[HorasExtrasTable.fechaRevision],

        comentario =
            this[HorasExtrasTable.comentario]
    )
}