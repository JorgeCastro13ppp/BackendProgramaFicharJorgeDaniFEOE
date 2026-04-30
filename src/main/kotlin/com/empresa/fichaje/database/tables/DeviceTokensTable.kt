package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object DeviceTokensTable : Table("device_tokens") {

    val token = varchar("token", 512)

    val userId = integer("user_id")
        .references(UsuariosTable.id)

    val platform = varchar("platform", 20)

    val updatedAt = long("updated_at")

    override val primaryKey =
        PrimaryKey(token)
}