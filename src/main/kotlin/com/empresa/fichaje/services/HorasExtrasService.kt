package com.empresa.fichaje.services

import com.empresa.fichaje.database.mappers.toHorasExtrasResponse
import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.dto.request.HorasExtrasFilter
import com.empresa.fichaje.dto.response.HorasExtrasResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class HorasExtrasService {

    private val estadosValidos =
        setOf("pendiente", "aprobado", "rechazado")


    /*
    ========================
    LISTADO PENDIENTES (ADMIN)
    ========================
    */

    fun obtenerPendientes(): List<HorasExtrasResponse> =
        transaction {

            HorasExtrasTable
                .selectAll()
                .where {
                    HorasExtrasTable.estado eq "pendiente"
                }
                .orderBy(
                    HorasExtrasTable.fecha to SortOrder.ASC
                )
                .map { it.toHorasExtrasResponse() }
        }


    /*
    ========================
    LISTADO POR USUARIO
    ========================
    */

    fun obtenerPorUsuario(
        userId: Int
    ): List<HorasExtrasResponse> =
        transaction {

            HorasExtrasTable
                .selectAll()
                .where {
                    HorasExtrasTable.userId eq userId
                }
                .orderBy(
                    HorasExtrasTable.fecha to SortOrder.DESC
                )
                .map { it.toHorasExtrasResponse() }
        }


    /*
    ========================
    BUSQUEDA FLEXIBLE (ADMIN)
    ========================
    */

    fun buscarHorasExtras(
        filter: HorasExtrasFilter
    ): List<HorasExtrasResponse> = transaction {

        HorasExtrasTable
            .selectAll()
            .apply {

                filter.estado?.let {

                    if (it in estadosValidos) {

                        andWhere {
                            HorasExtrasTable.estado eq it
                        }
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
            .orderBy(
                HorasExtrasTable.fecha to SortOrder.DESC
            )
            .map { it.toHorasExtrasResponse() }
    }


    /*
    ========================
    APROBAR / RECHAZAR EXTRA
    ========================
    */

    fun actualizarEstadoHorasExtra(
        id: Int,
        nuevoEstado: String,
        adminId: Int,
        comentario: String?
    ) = transaction {

        if (nuevoEstado !in estadosValidos)
            error("Estado inválido")

        HorasExtrasTable.update({
            HorasExtrasTable.id eq id
        }) {

            it[estado] = nuevoEstado

            it[aprobadoPor] = adminId

            it[fechaRevision] =
                System.currentTimeMillis()

            it[HorasExtrasTable.comentario] =
                comentario
        }
    }


    /*
    ========================
    RESUMEN USUARIO (APP)
    ========================
    */

    fun resumenUsuario(
        userId: Int
    ): Map<String, Any> = transaction {

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


    /*
    ========================
    RESUMEN GLOBAL EMPRESA (ADMIN)
    ========================
    */

    fun resumenEmpresa(): Map<String, Any> = transaction {

        val extras =
            HorasExtrasTable.selectAll()

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