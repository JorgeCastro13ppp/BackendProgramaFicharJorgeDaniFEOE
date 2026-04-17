package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object FichajesEventosTable : Table("fichajes_eventos") {

    val id = integer("id").autoIncrement()

    val userId = integer("user_id")
        .references(UsuariosTable.id)

    val timestamp = long("timestamp")

    val contexto = varchar("contexto", 20)

    val accion = varchar("accion", 20)

    val latitud = double("latitud")

    val longitud = double("longitud")

    val accuracy = double("accuracy")

    override val primaryKey = PrimaryKey(id)
}