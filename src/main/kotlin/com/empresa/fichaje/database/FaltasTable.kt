package com.empresa.fichaje.database

import org.jetbrains.exposed.sql.Table

object FaltasTable : Table("faltas") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val fecha = varchar("fecha", 20)
    val tipo = varchar("tipo", 20) // injustificada, justificada, retraso
    val descripcion = varchar("descripcion", 255)

    override val primaryKey = PrimaryKey(id)
}