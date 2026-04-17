package com.empresa.fichaje.services

import com.empresa.fichaje.database.mappers.toHorasExtrasResponse
import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.dto.request.HorasExtrasFilter
import com.empresa.fichaje.dto.response.HorasExtrasResponse
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class HorasExtrasService {

    fun obtenerPendientes(): List<HorasExtrasResponse> =
        transaction {

            HorasExtrasTable
                .selectAll()
                .where {
                    HorasExtrasTable.estado eq "pendiente"
                }
                .map { it.toHorasExtrasResponse() }
        }


    fun obtenerPorUsuario(
        userId: Int
    ): List<HorasExtrasResponse> =
        transaction {

            HorasExtrasTable
                .selectAll()
                .where {
                    HorasExtrasTable.userId eq userId
                }
                .map { it.toHorasExtrasResponse() }
        }

    fun buscarHorasExtras(
        filter: HorasExtrasFilter
    ): List<HorasExtrasResponse> = transaction {

        HorasExtrasTable
            .selectAll()
            .apply {

                filter.estado?.let {
                    andWhere {
                        HorasExtrasTable.estado eq it
                    }
                }

                filter.userId?.let {
                    andWhere {
                        HorasExtrasTable.userId eq it
                    }
                }

                filter.desde?.let {
                    andWhere {
                        HorasExtrasTable.fecha greaterEq it
                    }
                }

                filter.hasta?.let {
                    andWhere {
                        HorasExtrasTable.fecha lessEq it
                    }
                }
            }
            .map { row -> row.toHorasExtrasResponse() }
    }

    fun resumenUsuario(userId: Int) = transaction {

        val extras =
            HorasExtrasTable
                .selectAll()
                .where {
                    HorasExtrasTable.userId eq userId
                }

        val pendientes =
            extras.count {
                it[HorasExtrasTable.estado] == "pendiente"
            }

        val aprobadas =
            extras.count {
                it[HorasExtrasTable.estado] == "aprobado"
            }

        val rechazadas =
            extras.count {
                it[HorasExtrasTable.estado] == "rechazado"
            }

        val totalMinutos =
            extras
                .filter {
                    it[HorasExtrasTable.estado] == "aprobado"
                }
                .sumOf {
                    it[HorasExtrasTable.minutosExtra]
                }

        mapOf(
            "pendientes" to pendientes,
            "aprobadas" to aprobadas,
            "rechazadas" to rechazadas,
            "totalMinutos" to totalMinutos
        )
    }
}