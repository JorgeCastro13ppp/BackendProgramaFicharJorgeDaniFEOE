package com.empresa.fichaje.utils

import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.dto.response.FichajeResponse
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toFichajeResponse(): FichajeResponse {

    return FichajeResponse(
        id = this[FichajesEventosTable.id],
        userId = this[FichajesEventosTable.userId],
        username = this[UsuariosTable.username],
        fechaHora = this[FichajesEventosTable.timestamp],
        tipo =
            "${this[FichajesEventosTable.accion]} · ${this[FichajesEventosTable.contexto]}"
                .lowercase(),
        latitud = this[FichajesEventosTable.latitud],
        longitud = this[FichajesEventosTable.longitud],
        accuracy = this[FichajesEventosTable.accuracy]
    )
}