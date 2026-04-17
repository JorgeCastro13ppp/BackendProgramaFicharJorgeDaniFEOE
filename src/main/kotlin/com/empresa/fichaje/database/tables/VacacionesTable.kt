package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object VacacionesTable : Table("vacaciones") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", UsuariosTable.id)
    val fechaInicio = varchar("fecha_inicio", 20)
    val fechaFin = varchar("fecha_fin", 20)
    val estado = varchar("estado", 20) // pendiente, aprobado, rechazado
    val tipo = varchar("tipo", 20) // navidad | libre

    override val primaryKey = PrimaryKey(id)
}