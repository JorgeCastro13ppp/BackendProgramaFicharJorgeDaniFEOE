package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object VacacionesResumenTable : Table("vacaciones_resumen") {

    val id = integer("id").autoIncrement()

    val userId =
        reference("user_id", UsuariosTable.id)

    val anio =
        integer("anio")

    val diasNavidadUsados =
        integer("dias_navidad_usados").default(0)

    val diasLibresUsados =
        integer("dias_libres_usados").default(0)

    override val primaryKey =
        PrimaryKey(id)
}