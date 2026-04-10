package com.empresa.fichaje.services

import com.empresa.fichaje.database.FaltasTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.FaltaResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
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


    fun obtener(userId: Int, role: String): List<FaltaResponse> {

        return transaction {

            val query = FaltasTable
                .innerJoin(
                    UsuariosTable,
                    { FaltasTable.userId },
                    { UsuariosTable.id }
                )
                .selectAll()

            val filteredQuery = if (role == "admin") {

                query

            } else {

                query.where {
                    FaltasTable.userId eq userId
                }
            }

            filteredQuery.map {

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