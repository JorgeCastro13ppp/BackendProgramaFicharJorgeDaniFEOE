package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.FaltasTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.domain.enums.Role
import com.empresa.fichaje.dto.response.FaltaResponse
import com.empresa.fichaje.utils.toTipoFaltaOrNull
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FaltasService {

    fun registrar(
        userId: Int,
        fecha: String,
        tipo: String,
        descripcion: String
    ) = transaction {

        val tipoEnum =
            tipo.toTipoFaltaOrNull()
                ?: throw IllegalArgumentException(
                    "Tipo de falta inválido"
                )


        val existeFaltaEseDia =
            FaltasTable
                .selectAll()
                .where {

                    (FaltasTable.userId eq userId) and
                            (FaltasTable.fecha eq fecha)
                }
                .count() > 0


        if (existeFaltaEseDia) {

            throw IllegalArgumentException(
                "El usuario ya tiene una falta registrada ese día"
            )
        }


        FaltasTable.insert {

            it[FaltasTable.userId] = userId
            it[FaltasTable.fecha] = fecha
            it[FaltasTable.tipo] =
                tipoEnum.name.lowercase()

            it[FaltasTable.descripcion] = descripcion
        }
    }


    fun obtener(
        userId: Int,
        role: Role,
        tipo: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<FaltaResponse> = transaction {

        val tipoEnum =
            tipo?.toTipoFaltaOrNull()


        val query =
            FaltasTable
                .innerJoin(UsuariosTable)
                .selectAll()
                .apply {

                    if (role != Role.ADMIN) {

                        andWhere {
                            FaltasTable.userId eq userId
                        }
                    }

                    tipoEnum?.let {

                        andWhere {
                            FaltasTable.tipo eq it.name.lowercase()
                        }
                    }
                }


        val sortColumn =
            when (sortBy) {

                "fecha" -> FaltasTable.fecha
                "usuario" -> UsuariosTable.username
                "tipo" -> FaltasTable.tipo
                else -> FaltasTable.fecha
            }


        val sortOrder =
            if (order == "desc")
                SortOrder.DESC
            else
                SortOrder.ASC


        query
            .orderBy(sortColumn to sortOrder)
            .map {

                FaltaResponse(
                    id = it[FaltasTable.id],
                    userId = it[FaltasTable.userId],
                    username = it[UsuariosTable.username],
                    fecha = it[FaltasTable.fecha],
                    tipo = it[FaltasTable.tipo],
                    descripcion = it[FaltasTable.descripcion]
                )
            }
    }


    fun eliminar(
        id: Int
    ) = transaction {

        FaltasTable.deleteWhere {

            FaltasTable.id eq id
        }
    }
}