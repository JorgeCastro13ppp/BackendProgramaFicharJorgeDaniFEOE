package com.empresa.fichaje.services

import com.empresa.fichaje.database.FaltasTable
import com.empresa.fichaje.models.FaltaResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere

class FaltasService {

    fun registrar(
        userId: Int,
        fecha: String,
        tipo: String,
        descripcion: String
    ) {

        transaction {

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
        role: String
    ): List<FaltaResponse> {

        return transaction {

            val query = if (role == "admin") {
                FaltasTable.selectAll()
            } else {
                FaltasTable.selectAll().filter {
                    it[FaltasTable.userId] == userId
                }
            }

            query.map {

                FaltaResponse(
                    id = it[FaltasTable.id],
                    userId = it[FaltasTable.userId],
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