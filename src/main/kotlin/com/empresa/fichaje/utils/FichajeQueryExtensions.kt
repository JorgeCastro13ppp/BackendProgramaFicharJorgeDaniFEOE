package com.empresa.fichaje.utils

import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.dto.response.FichajeResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun ColumnSet.latestByUserId(
    userId: Int
): Query {

    return this
        .selectAll()
        .where {
            FichajesEventosTable.userId eq userId
        }
        .orderBy(
            FichajesEventosTable.timestamp,
            SortOrder.DESC
        )
        .limit(1)
}


fun ColumnSet.latestFichajeResponse(
    userId: Int
): FichajeResponse? {

    return latestByUserId(userId)
        .map { it.toFichajeResponse() }
        .firstOrNull()
}

fun ColumnSet.eventsBetweenDatesByUser(
    userId: Int,
    inicio: Long,
    fin: Long
): Query {

    return this
        .selectAll()
        .where {

            (FichajesEventosTable.userId eq userId) and
                    (FichajesEventosTable.timestamp greaterEq inicio) and
                    (FichajesEventosTable.timestamp lessEq fin)
        }
}

fun ColumnSet.eventsBetweenDatesByUserOrdered(
    userId: Int,
    inicio: Long,
    fin: Long
): Query {

    return eventsBetweenDatesByUser(
        userId,
        inicio,
        fin
    ).orderBy(
        FichajesEventosTable.timestamp,
        SortOrder.ASC
    )
}

fun ColumnSet.eventsBetweenDatesResponse(
    userId: Int,
    inicio: Long,
    fin: Long
): List<FichajeResponse> {

    return eventsBetweenDatesByUser(
        userId,
        inicio,
        fin
    ).map {
        it.toFichajeResponse()
    }
}