package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object DocumentosTable : Table("documentos") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", UsuariosTable.id)
    val nombre = varchar("nombre", 100)
    val tipo = varchar("tipo", 50) // nomina, epi, diploma...
    val url = varchar("url", 255) // ruta del archivo

    override val primaryKey = PrimaryKey(id)
}