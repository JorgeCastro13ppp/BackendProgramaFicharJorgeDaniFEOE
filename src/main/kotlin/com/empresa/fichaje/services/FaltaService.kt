package com.empresa.fichaje.services

import com.empresa.fichaje.database.FaltasTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.FaltaResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select


class FaltasService {

    fun registrar(
        userId: Int,
        fecha: String,
        tipo: String,
        descripcion: String
    ) {

        transaction {

            val existeFaltaEseDia =

                FaltasTable
                    .select {

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
                it[FaltasTable.tipo] = tipo
                it[FaltasTable.descripcion] = descripcion
            }
        }
    }


    fun obtener(
        userId: Int,
        role: String,
        tipo: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<FaltaResponse> {

        return transaction {

            var query =
                FaltasTable
                    .innerJoin(
                        UsuariosTable,
                        { FaltasTable.userId },
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
                        FaltasTable.userId eq userId
                    }
            }


            /*
            ========================
            FILTRO POR TIPO
            ========================
            */

            if (tipo != null) {

                query =
                    query.andWhere {
                        FaltasTable.tipo eq tipo
                    }
            }


            /*
            ========================
            ORDENACIÓN
            ========================
            */

            val sortColumn = when (sortBy) {

                "fecha" -> FaltasTable.fecha
                "usuario" -> UsuariosTable.username
                "tipo" -> FaltasTable.tipo
                "id" -> FaltasTable.id

                else -> FaltasTable.fecha
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
    }


    fun eliminar(id: Int) {

        transaction {

            FaltasTable.deleteWhere {

                FaltasTable.id eq id
            }
        }
    }
}