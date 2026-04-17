package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object FaltasTable : Table("faltas") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", UsuariosTable.id)
    val fecha = varchar("fecha", 20)
    val tipo = varchar("tipo", 20) // injustificada, justificada, retraso
    val descripcion = varchar("descripcion", 255)

    override val primaryKey = PrimaryKey(id)
}