package com.empresa.fichaje.services

import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.database.VacacionesTable
import com.empresa.fichaje.models.VacacionesRequest
import com.empresa.fichaje.models.VacacionesResponse
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class VacacionesService {

    fun solicitar(
        userId: Int,
        fechaInicio: String,
        fechaFin: String
    ) {

        transaction {

            VacacionesTable.insert {

                it[VacacionesTable.userId] = userId
                it[VacacionesTable.fechaInicio] = fechaInicio
                it[VacacionesTable.fechaFin] = fechaFin
                it[VacacionesTable.estado] = "pendiente"
            }
        }
    }

    fun obtener(
        userId: Int,
        role: String
    ): List<VacacionesResponse> {

        return transaction {

            val query = if (role == "admin") {

                VacacionesTable
                    .innerJoin(
                        UsuariosTable,
                        { VacacionesTable.userId },
                        { UsuariosTable.id }
                    )
                    .selectAll()

            } else {

                VacacionesTable
                    .innerJoin(
                        UsuariosTable,
                        { VacacionesTable.userId },
                        { UsuariosTable.id }
                    )
                    .select {
                        VacacionesTable.userId eq userId
                    }
            }

            query.map {

                VacacionesResponse(
                    id = it[VacacionesTable.id],
                    userId = it[VacacionesTable.userId],
                    username = it[UsuariosTable.username],
                    fechaInicio = it[VacacionesTable.fechaInicio],
                    fechaFin = it[VacacionesTable.fechaFin],
                    estado = it[VacacionesTable.estado]
                )
            }
        }
    }

    fun actualizarEstado(id: Int, nuevoEstado: String) {
        transaction {
            VacacionesTable.update({ VacacionesTable.id eq id }) {
                it[estado] = nuevoEstado
            }
        }
    }
}