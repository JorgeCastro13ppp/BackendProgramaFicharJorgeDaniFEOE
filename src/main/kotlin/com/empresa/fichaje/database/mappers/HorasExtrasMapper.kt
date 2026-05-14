package com.empresa.fichaje.database.mappers

import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.dto.response.HorasExtrasResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

fun ResultRow.toHorasExtrasResponse(): HorasExtrasResponse {

    val aprobadoPorId =
        this[HorasExtrasTable.aprobadoPor]


    /*
    ========================
    USERNAME ADMIN
    ========================
    */

    val aprobadoPorUsername =

        aprobadoPorId?.let { adminId ->

            UsuariosTable
                .selectAll()
                .where {
                    UsuariosTable.id eq adminId
                }
                .firstOrNull()
                ?.get(UsuariosTable.username)
        }


    return HorasExtrasResponse(

        id =
            this[HorasExtrasTable.id],

        userId =
            this[HorasExtrasTable.userId],

        username =
            this[UsuariosTable.username],

        fecha =
            this[HorasExtrasTable.fecha],

        minutosExtra =
            this[HorasExtrasTable.minutosExtra],

        estado =
            this[HorasExtrasTable.estado],

        aprobadoPor =
            aprobadoPorId,

        aprobadoPorUsername =
            aprobadoPorUsername,

        fechaRevision =
            this[HorasExtrasTable.fechaRevision],

        comentario =
            this[HorasExtrasTable.comentario]
    )
}