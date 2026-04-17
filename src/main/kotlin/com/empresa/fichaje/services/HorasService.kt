package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.database.tables.JornadasLaboralesTable
import com.empresa.fichaje.domain.enums.AccionFichaje
import com.empresa.fichaje.dto.response.HorasDiaResponse
import com.empresa.fichaje.utils.dailyTimeline
import com.empresa.fichaje.utils.todayRange
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll





class HorasService {

    fun calcularHoras(
        userId: Int,
        fechaInicio: Long,
        fechaFin: Long
    ): HorasDiaResponse = transaction {

        val eventos =
            FichajesEventosTable
                .selectAll()
                .where {

                    (FichajesEventosTable.userId eq userId) and
                            (FichajesEventosTable.timestamp greaterEq fechaInicio) and
                            (FichajesEventosTable.timestamp lessEq fechaFin)
                }
                .orderBy(FichajesEventosTable.timestamp to SortOrder.ASC)
                .toList()


        if (eventos.isEmpty())
            return@transaction HorasDiaResponse(0, 0, 0, 0)


        var inicioTrabajo: Long? = null
        var inicioViaje: Long? = null
        var inicioDescanso: Long? = null

        var tiempoTrabajo = 0L
        var tiempoViaje = 0L
        var tiempoDescanso = 0L


        eventos.forEach { row ->

            val timestamp =
                row[FichajesEventosTable.timestamp]

            val accion =
                AccionFichaje.valueOf(
                    row[FichajesEventosTable.accion]
                )


            when (accion) {

                AccionFichaje.ENTRADA ->
                    inicioTrabajo = timestamp


                AccionFichaje.SALIDA ->
                    inicioTrabajo?.let {

                        tiempoTrabajo += timestamp - it
                        inicioTrabajo = null
                    }


                AccionFichaje.INICIO_VIAJE ->
                    inicioViaje = timestamp


                AccionFichaje.FIN_VIAJE ->
                    inicioViaje?.let {

                        tiempoViaje += timestamp - it
                        inicioViaje = null
                    }


                AccionFichaje.INICIO_DESCANSO ->
                    inicioDescanso = timestamp


                AccionFichaje.FIN_DESCANSO ->
                    inicioDescanso?.let {

                        tiempoDescanso += timestamp - it
                        inicioDescanso = null
                    }
            }
        }


        /*
        ========================
        CIERRE INTERVALOS ABIERTOS
        ========================
        */

        inicioTrabajo?.let {
            tiempoTrabajo += fechaFin - it
        }

        inicioViaje?.let {
            tiempoViaje += fechaFin - it
        }

        inicioDescanso?.let {
            tiempoDescanso += fechaFin - it
        }


        /*
        ========================
        TIEMPO TOTAL REAL
        ========================
        */

        val tiempoTotal =
            tiempoTrabajo + tiempoViaje + tiempoDescanso


        HorasDiaResponse(
            tiempoTotal = tiempoTotal,
            tiempoTrabajo = tiempoTrabajo,
            tiempoViaje = tiempoViaje,
            tiempoDescanso = tiempoDescanso
        )
    }

    fun calcularHorasTrabajadasHoy(userId: Int): Long {

        val (inicioDia, finDia) = todayRange()

        val timeline = transaction {

            FichajesEventosTable.dailyTimeline(
                userId,
                inicioDia,
                finDia
            )
        }

        return timeline.totalTrabajo()
    }

    fun resumenHorasHoy(userId: Int): HorasDiaResponse {

        val (inicioDia, finDia) = todayRange()

        val timeline = transaction {

            FichajesEventosTable.dailyTimeline(
                userId,
                inicioDia,
                finDia
            )
        }

        return HorasDiaResponse(

            tiempoTotal = timeline.tiempoTotalJornada(),

            tiempoTrabajo = timeline.totalTrabajo(),

            tiempoViaje = timeline.totalViaje(),

            tiempoDescanso = timeline.totalDescanso()
        )
    }

    fun calcularJornadaLegal(
        userId: Int,
        timestampSalida: Long?
    ) = transaction {

        val zona = ZoneId.systemDefault()

        val fecha: LocalDate =
            Instant.ofEpochMilli(
                timestampSalida ?: System.currentTimeMillis()
            )
                .atZone(zona)
                .toLocalDate()

        val fechaStr = fecha.toString()


        val yaProcesada =
            JornadasLaboralesTable
                .selectAll()
                .where {
                    (JornadasLaboralesTable.userId eq userId) and
                            (JornadasLaboralesTable.fecha eq fechaStr) and
                            (JornadasLaboralesTable.procesada eq true)
                }
                .count() > 0


        if (yaProcesada)
            return@transaction


        val inicioDia =
            fecha.atStartOfDay(zona)
                .toInstant()
                .toEpochMilli()


        val finDia =
            fecha.plusDays(1)
                .atStartOfDay(zona)
                .toInstant()
                .toEpochMilli()


        val timeline =
            FichajesEventosTable.dailyTimeline(
                userId,
                inicioDia,
                finDia
            )


        val entradaReal =
            timeline.firstEntrada()

        val salidaReal =
            timeline.lastSalida()


        if (entradaReal == null)
            return@transaction


        val inicioLegalEmpresa =
            fecha.atTime(7, 0)
                .atZone(zona)
                .toInstant()
                .toEpochMilli()


        val finLegalEmpresa =
            fecha.atTime(15, 0)
                .atZone(zona)
                .toInstant()
                .toEpochMilli()


        val entradaLegal =
            if (entradaReal > inicioLegalEmpresa)
                entradaReal
            else
                inicioLegalEmpresa


        val salidaLegal =
            when {
                salidaReal == null -> finLegalEmpresa
                salidaReal > finLegalEmpresa -> finLegalEmpresa
                else -> salidaReal
            }


        val tiempoLegal =
            salidaLegal - entradaLegal


        val tiempoExtraDetectado =
            if (salidaReal != null && salidaReal > finLegalEmpresa)
                salidaReal - finLegalEmpresa
            else
                0L


        val tiempoTrabajoReal =
            timeline.totalTrabajo()

        val tiempoViajeReal =
            timeline.totalViaje()

        val tiempoDescansoReal =
            timeline.totalDescanso()


        JornadasLaboralesTable.insert {

            it[JornadasLaboralesTable.userId] =
                userId

            it[JornadasLaboralesTable.fecha] =
                fechaStr

            it[JornadasLaboralesTable.entradaReal] =
                entradaReal

            it[JornadasLaboralesTable.salidaReal] =
                salidaReal

            it[JornadasLaboralesTable.entradaLegal] =
                entradaLegal

            it[JornadasLaboralesTable.salidaLegal] =
                salidaLegal

            it[JornadasLaboralesTable.tiempoTrabajoReal] =
                tiempoTrabajoReal

            it[JornadasLaboralesTable.tiempoViajeReal] =
                tiempoViajeReal

            it[JornadasLaboralesTable.tiempoDescansoReal] =
                tiempoDescansoReal

            it[JornadasLaboralesTable.tiempoLegal] =
                tiempoLegal

            it[JornadasLaboralesTable.tiempoExtraDetectado] =
                tiempoExtraDetectado

            it[JornadasLaboralesTable.cerradaAutomaticamente] =
                (salidaReal == null)

            it[JornadasLaboralesTable.procesada] =
                true
        }


        if (tiempoExtraDetectado > 0L) {

            val yaExiste =
                HorasExtrasTable
                    .selectAll()
                    .where {
                        (HorasExtrasTable.userId eq userId) and
                                (HorasExtrasTable.fecha eq fechaStr)
                    }
                    .count() > 0


            if (!yaExiste) {

                HorasExtrasTable.insert {

                    it[HorasExtrasTable.userId] =
                        userId

                    it[HorasExtrasTable.fecha] =
                        fechaStr

                    it[HorasExtrasTable.minutosExtra] =
                        tiempoExtraDetectado

                    it[HorasExtrasTable.estado] =
                        "pendiente"
                }
            }
        }
    }
    fun actualizarEstadoHorasExtra(
        id: Int,
        nuevoEstado: String,
        adminId: Int,
        comentario: String?
    ) = transaction {

        val estadoValido =
            listOf("pendiente", "aprobado", "rechazado")

        if (nuevoEstado !in estadoValido)
            error("Estado inválido")


        HorasExtrasTable.update({
            HorasExtrasTable.id eq id
        }) {

            it[estado] = nuevoEstado
            it[aprobadoPor] = adminId
            it[fechaRevision] = System.currentTimeMillis()
            it[HorasExtrasTable.comentario] = comentario
        }
    }
}