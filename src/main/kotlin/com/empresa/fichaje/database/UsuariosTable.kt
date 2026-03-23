package com.empresa.fichaje.database

import org.jetbrains.exposed.sql.Table

object UsuariosTable : Table("usuarios") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val password = varchar("password", 100)
    val role = varchar("role", 20)

    override val primaryKey = PrimaryKey(id)
}