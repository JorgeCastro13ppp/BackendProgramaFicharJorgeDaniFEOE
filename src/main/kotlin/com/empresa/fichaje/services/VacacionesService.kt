package com.empresa.fichaje.services

import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.database.VacacionesTable
import com.empresa.fichaje.models.VacacionesRequest
import com.empresa.fichaje.models.VacacionesResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
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

            val existeSolapamiento =
                VacacionesTable
                    .select {
                        (VacacionesTable.userId eq userId) and
                                (
                                        (VacacionesTable.fechaInicio lessEq fechaFin) and
                                                (VacacionesTable.fechaFin greaterEq fechaInicio)
                                        )
                    }
                    .count() > 0


            if (existeSolapamiento) {

                throw IllegalArgumentException(
                    "El usuario ya tiene vacaciones en ese periodo"
                )
            }


            VacacionesTable.insert {

                it[VacacionesTable.userId] = userId
                it[VacacionesTable.fechaInicio] = fechaInicio
                it[VacacionesTable.fechaFin] = fechaFin
                it[estado] = "pendiente"
            }
        }
    }

    fun obtener(
        userId: Int,
        role: String,
        estado: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<VacacionesResponse> {

        return transaction {

            var query =
                VacacionesTable
                    .innerJoin(
                        UsuariosTable,
                        { VacacionesTable.userId },
                        { UsuariosTable.id }
                    )
                    .selectAll()


            /*
            ========================
            FILTRO POR ROL
            ========================
            */

            if (role != "admin") {

                query =
                    query.andWhere {
                        VacacionesTable.userId eq userId
                    }
            }


            /*
            ========================
            FILTRO POR ESTADO
            ========================
            */

            if (estado != null) {

                query =
                    query.andWhere {
                        VacacionesTable.estado eq estado
                    }
            }


            /*
            ========================
            ORDENACIÓN
            ========================
            */

            val sortColumn = when (sortBy) {

                "inicio" -> VacacionesTable.fechaInicio
                "fin" -> VacacionesTable.fechaFin
                "usuario" -> UsuariosTable.username
                "estado" -> VacacionesTable.estado
                "id" -> VacacionesTable.id

                else -> VacacionesTable.fechaInicio
            }


            val sortOrder =
                if (order == "desc")
                    SortOrder.DESC
                else
                    SortOrder.ASC


            query =
                query.orderBy(sortColumn to sortOrder)


            /*
            ========================
            RESPUESTA FINAL
            ========================
            */

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