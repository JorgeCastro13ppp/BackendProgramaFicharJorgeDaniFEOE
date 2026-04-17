package com.empresa.fichaje.utils

import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.domain.enums.AccionFichaje
import com.empresa.fichaje.domain.models.TimelineEvent
import com.empresa.fichaje.domain.models.UserDailyTimeline
import org.jetbrains.exposed.sql.ColumnSet

fun ColumnSet.dailyTimeline(

    userId: Int,
    inicio: Long,
    fin: Long

): UserDailyTimeline {

    val eventos = eventsBetweenDatesByUserOrdered(
        userId,
        inicio,
        fin
    ).map {

        TimelineEvent(
            accion =
                AccionFichaje.valueOf(
                    it[FichajesEventosTable.accion]
                ),
            timestamp =
                it[FichajesEventosTable.timestamp]
        )
    }

    return UserDailyTimeline(
        userId = userId,
        eventos = eventos
    )
}