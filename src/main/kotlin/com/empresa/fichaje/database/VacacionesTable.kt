package com.empresa.fichaje.database

import org.jetbrains.exposed.sql.Table

object VacacionesTable : Table("vacaciones") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val fechaInicio = varchar("fecha_inicio", 20)
    val fechaFin = varchar("fecha_fin", 20)
    val estado = varchar("estado", 20) // pendiente, aprobado, rechazado

    override val primaryKey = PrimaryKey(id)
}