package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object HorasExtrasTable : Table("horas_extras") {

    val id =
        integer("id").autoIncrement()

    val userId =
        integer("user_id")

    val fecha =
        varchar("fecha", 10)

    val minutosExtra =
        long("minutos_extra")

    val estado =
        varchar("estado", 20)

    val aprobadoPor =
        integer("aprobado_por")
            .nullable()

    val fechaRevision =
        long("fecha_revision")
            .nullable()

    val comentario =
        varchar("comentario", 255)
            .nullable()

    override val primaryKey =
        PrimaryKey(id)
}