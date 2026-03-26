package com.empresa.fichaje.database

import org.jetbrains.exposed.sql.Table

object FichajesTable : Table("fichajes") {
    val id = integer("id").autoIncrement()
    val userId = reference(
        "user_id",
        UsuariosTable.id
    )
    val fechaHora = long("fecha_hora")
    val tipo = varchar("tipo", 10) // entrada / salida

    override val primaryKey = PrimaryKey(id)
}