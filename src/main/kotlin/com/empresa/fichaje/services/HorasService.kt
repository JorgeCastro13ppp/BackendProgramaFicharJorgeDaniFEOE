package com.empresa.fichaje.services

import com.empresa.fichaje.config.EmpresaConfig
import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.database.tables.JornadasLaboralesTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.domain.enums.AccionFichaje
import com.empresa.fichaje.dto.response.HorasDiaResponse
import com.empresa.fichaje.dto.response.JornadaIncidenciaResponse
import com.empresa.fichaje.dto.response.JornadaPendienteRevisionResponse
import com.empresa.fichaje.dto.response.JornadaResponse
import com.empresa.fichaje.dto.response.JornadaResumenGlobalResponse
import com.empresa.fichaje.dto.response.JornadaResumenMensualResponse
import com.empresa.fichaje.utils.dailyTimeline
import com.empresa.fichaje.utils.todayRange
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


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
                .orderBy(
                    FichajesEventosTable.timestamp to SortOrder.ASC
                )
                .toList()


        if (eventos.isEmpty())
            return@transaction HorasDiaResponse(0,0,0,0)


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


        inicioTrabajo?.let { tiempoTrabajo += fechaFin - it }
        inicioViaje?.let { tiempoViaje += fechaFin - it }
        inicioDescanso?.let { tiempoDescanso += fechaFin - it }


        val tiempoTotal =
            tiempoTrabajo + tiempoViaje + tiempoDescanso


        HorasDiaResponse(
            tiempoTotal,
            tiempoTrabajo,
            tiempoViaje,
            tiempoDescanso
        )
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
            timeline.tiempoTotalJornada(),
            timeline.totalTrabajo(),
            timeline.totalViaje(),
            timeline.totalDescanso()
        )
    }


    fun calcularJornadaLegal(
        userId: Int,
        timestampSalida: Long?
    ) = transaction {

        val zona = ZoneId.systemDefault()

        val fecha =
            Instant.ofEpochMilli(
                timestampSalida ?: System.currentTimeMillis()
            )
                .atZone(zona)
                .toLocalDate()

        val fechaStr = fecha.toString()


        /*
        ========================
        EVITAR DUPLICADOS
        ========================
        */

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


        /*
        ========================
        TIMELINE DEL DÍA
        ========================
        */

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


        /*
        ========================
        ENTRADA REAL
        ========================
        */

        val entradaReal =
            timeline.firstEntrada()
                ?: return@transaction


        /*
        ========================
        JORNADA LEGAL EMPRESA
        ========================
        */

        val inicioLegalEmpresa =
            fecha.atTime(
                EmpresaConfig.HORA_INICIO_JORNADA,
                EmpresaConfig.MINUTO_INICIO_JORNADA
            )
                .atZone(zona)
                .toInstant()
                .toEpochMilli()

        val finLegalEmpresa =
            fecha.atTime(
                EmpresaConfig.HORA_INICIO_JORNADA,
                EmpresaConfig.MINUTO_INICIO_JORNADA
            )
                .plusHours(
                    EmpresaConfig.DURACION_JORNADA_HORAS
                )
                .atZone(zona)
                .toInstant()
                .toEpochMilli()


        /*
        ========================
        SALIDA REAL (AUTO-CIERRE)
        ========================
        */

        var salidaReal =
            timeline.lastSalida()

        var salidaAutomatica = false


        if (salidaReal == null) {

            salidaReal = finLegalEmpresa

            salidaAutomatica = true
        }


        if (salidaReal <= entradaReal)
            return@transaction


        /*
        ========================
        ENTRADA LEGAL
        ========================
        */

        val entradaLegal =
            maxOf(entradaReal, inicioLegalEmpresa)


        /*
        ========================
        SALIDA LEGAL
        ========================
        */

        val salidaLegal =
            minOf(salidaReal, finLegalEmpresa)


        /*
        ========================
        DESCANSOS
        ========================
        */

        val tiempoDescansoReal =
            timeline.totalDescanso()


        /*
        ========================
        TIEMPO LEGAL
        ========================
        */

        val tiempoLegal =
            maxOf(
                0L,
                (salidaLegal - entradaLegal) - tiempoDescansoReal
            )


        /*
        ========================
        HORAS EXTRA
        ========================
        */

        val tiempoExtraDetectado =
            if (!salidaAutomatica && salidaReal > finLegalEmpresa)
                salidaReal - finLegalEmpresa
            else
                0L


        val minutosExtra =
            tiempoExtraDetectado / 60000


        /*
        ========================
        INSERTAR JORNADA
        ========================
        */

        JornadasLaboralesTable.insert {

            it[JornadasLaboralesTable.userId] = userId
            it[JornadasLaboralesTable.fecha] = fechaStr
            it[JornadasLaboralesTable.entradaReal] = entradaReal
            it[JornadasLaboralesTable.salidaReal] = salidaReal
            it[JornadasLaboralesTable.entradaLegal] = entradaLegal
            it[JornadasLaboralesTable.salidaLegal] = salidaLegal

            it[JornadasLaboralesTable.tiempoTrabajoReal] =
                timeline.totalTrabajo()

            it[JornadasLaboralesTable.tiempoViajeReal] =
                timeline.totalViaje()

            it[JornadasLaboralesTable.tiempoDescansoReal] =
                tiempoDescansoReal

            it[JornadasLaboralesTable.tiempoLegal] =
                tiempoLegal

            it[JornadasLaboralesTable.tiempoExtraDetectado] =
                tiempoExtraDetectado

            it[JornadasLaboralesTable.cerradaAutomaticamente] =
                salidaAutomatica

            it[JornadasLaboralesTable.procesada] =
                true
        }


        /*
        ========================
        INSERTAR HORAS EXTRA
        ========================
        */

        if (minutosExtra > 0) {

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

                    it[HorasExtrasTable.userId] = userId
                    it[HorasExtrasTable.fecha] = fechaStr
                    it[HorasExtrasTable.minutosExtra] = minutosExtra
                    it[HorasExtrasTable.estado] = "pendiente"
                }
            }
        }
    }

    fun cerrarJornadaAnteriorSiExiste(userId: Int) = transaction {

        val ultimoEvento =
            FichajesEventosTable
                .select {
                    FichajesEventosTable.userId eq userId
                }
                .orderBy(
                    FichajesEventosTable.timestamp,
                    SortOrder.DESC
                )
                .limit(1)
                .firstOrNull()
                ?: return@transaction


        val accion =
            AccionFichaje.valueOf(
                ultimoEvento[FichajesEventosTable.accion]
            )


        if (accion != AccionFichaje.ENTRADA)
            return@transaction


        val timestamp =
            ultimoEvento[FichajesEventosTable.timestamp]


        calcularJornadaLegal(
            userId,
            timestampSalida = null
        )
    }

    fun cerrarJornadasAbiertasDelDiaAnterior() = transaction {

        val zona = ZoneId.systemDefault()

        val ayer =
            LocalDate.now(zona).minusDays(1)

        val inicioAyer =
            ayer.atStartOfDay(zona)
                .toInstant()
                .toEpochMilli()

        val finAyer =
            ayer.plusDays(1)
                .atStartOfDay(zona)
                .toInstant()
                .toEpochMilli()


        /*
        ========================
        USUARIOS CON EVENTOS AYER
        ========================
        */

        val usuarios =
            FichajesEventosTable
                .selectAll()
                .where {
                    (FichajesEventosTable.timestamp greaterEq inicioAyer) and
                            (FichajesEventosTable.timestamp less finAyer)
                }
                .map { it[FichajesEventosTable.userId] }
                .distinct()


        /*
        ========================
        CERRAR JORNADAS ABIERTAS
        ========================
        */

        usuarios.forEach { userId ->

            val ultimoEventoAyer =
                FichajesEventosTable
                    .selectAll()
                    .where {
                        (FichajesEventosTable.userId eq userId) and
                                (FichajesEventosTable.timestamp greaterEq inicioAyer) and
                                (FichajesEventosTable.timestamp less finAyer)
                    }
                    .orderBy(
                        FichajesEventosTable.timestamp to SortOrder.DESC
                    )
                    .limit(1)
                    .firstOrNull()
                    ?: return@forEach


            val accion =
                AccionFichaje.valueOf(
                    ultimoEventoAyer[FichajesEventosTable.accion]
                )


            /*
            ========================
            SI TERMINÓ EN ENTRADA → JORNADA ABIERTA
            ========================
            */

            if (accion == AccionFichaje.ENTRADA) {

                calcularJornadaLegal(
                    userId,
                    timestampSalida = null
                )
            }
        }
    }

    fun corregirSalidaJornada(
        jornadaId: Int,
        nuevaSalidaReal: Long,
        adminId: Int,
        comentario: String?
    ) = transaction {

        val jornada =
            JornadasLaboralesTable
                .selectAll()
                .where {
                    JornadasLaboralesTable.id eq jornadaId
                }
                .firstOrNull()
                ?: error("Jornada no encontrada")


        val userId =
            jornada[JornadasLaboralesTable.userId]

        val fecha =
            jornada[JornadasLaboralesTable.fecha]


        /*
        ========================
        ACTUALIZAR SALIDA REAL
        ========================
        */

        JornadasLaboralesTable.update({
            JornadasLaboralesTable.id eq jornadaId
        }) {

            it[salidaReal] = nuevaSalidaReal
            it[cerradaAutomaticamente] = false
            it[corregidaPor] = adminId
            it[comentarioAdmin] = comentario
            it[fechaCorreccion] = System.currentTimeMillis()
        }


        /*
        ========================
        ELIMINAR HORAS EXTRA ANTIGUAS
        ========================
        */

        HorasExtrasTable.deleteWhere {

            (HorasExtrasTable.userId eq userId) and
                    (HorasExtrasTable.fecha eq fecha)
        }


        /*
        ========================
        RECALCULAR JORNADA COMPLETA
        ========================
        */

        calcularJornadaLegal(
            userId,
            nuevaSalidaReal
        )
    }

    fun obtenerJornadasCerradasAutomaticamente():
            List<JornadaPendienteRevisionResponse> = transaction {

        JornadasLaboralesTable
            .selectAll()
            .where {
                JornadasLaboralesTable.cerradaAutomaticamente eq true
            }
            .map {

                JornadaPendienteRevisionResponse(

                    id = it[JornadasLaboralesTable.id],

                    userId = it[JornadasLaboralesTable.userId],

                    username = it[UsuariosTable.username],

                    fecha = it[JornadasLaboralesTable.fecha],

                    entradaReal =
                        it[JornadasLaboralesTable.entradaReal],

                    salidaReal =
                        it[JornadasLaboralesTable.salidaReal],

                    cerradaAutomaticamente =
                        it[JornadasLaboralesTable.cerradaAutomaticamente],

                    tiempoExtraDetectado =
                        it[JornadasLaboralesTable.tiempoExtraDetectado]
                )
            }
    }

    fun obtenerJornadasPorUsuario(
        userId: Int,
        desde: String? = null,
        hasta: String? = null
    ): List<JornadaResponse> = transaction {

        JornadasLaboralesTable
            .selectAll()
            .apply {

                andWhere {
                    JornadasLaboralesTable.userId eq userId
                }

                desde?.let {

                    andWhere {
                        JornadasLaboralesTable.fecha greaterEq it
                    }
                }

                hasta?.let {

                    andWhere {
                        JornadasLaboralesTable.fecha lessEq it
                    }
                }
            }
            .orderBy(
                JornadasLaboralesTable.fecha to SortOrder.DESC
            )
            .map {

                JornadaResponse(
                    id = it[JornadasLaboralesTable.id],
                    fecha = it[JornadasLaboralesTable.fecha],
                    entradaReal = it[JornadasLaboralesTable.entradaReal],
                    salidaReal = it[JornadasLaboralesTable.salidaReal],
                    tiempoLegal = it[JornadasLaboralesTable.tiempoLegal],
                    tiempoExtraDetectado =
                        it[JornadasLaboralesTable.tiempoExtraDetectado],
                    cerradaAutomaticamente =
                        it[JornadasLaboralesTable.cerradaAutomaticamente],
                    corregidaPor =
                        it[JornadasLaboralesTable.corregidaPor],
                    comentarioAdmin =
                        it[JornadasLaboralesTable.comentarioAdmin],
                    fechaCorreccion =
                        it[JornadasLaboralesTable.fechaCorreccion]
                )
            }
    }

    fun obtenerResumenMensual(
        userId: Int,
        mes: String
    ): JornadaResumenMensualResponse = transaction {

        val jornadas =

            JornadasLaboralesTable
                .selectAll()
                .where {

                    (JornadasLaboralesTable.userId eq userId) and

                            (JornadasLaboralesTable.fecha like "$mes%")
                }


        val totalJornadas = jornadas.count()


        val jornadasAutomaticas = jornadas.count {

            it[JornadasLaboralesTable.cerradaAutomaticamente]
        }


        val jornadasCorregidas = jornadas.count {

            it[JornadasLaboralesTable.corregidaPor] != null
        }


        val totalTiempoLegal =

            jornadas.sumOf {

                it[JornadasLaboralesTable.tiempoLegal]
            }


        val totalTiempoExtra =

            jornadas.sumOf {

                it[JornadasLaboralesTable.tiempoExtraDetectado]
            }


        JornadaResumenMensualResponse(

            userId = userId,

            mes = mes,

            totalJornadas = totalJornadas,

            jornadasAutomaticas = jornadasAutomaticas,

            jornadasCorregidas = jornadasCorregidas,

            totalTiempoLegal = totalTiempoLegal,

            totalTiempoExtra = totalTiempoExtra
        )
    }

    fun obtenerResumenGlobalMensual(
        mes: String
    ): JornadaResumenGlobalResponse = transaction {

        val jornadas =

            JornadasLaboralesTable
                .selectAll()
                .where {
                    JornadasLaboralesTable.fecha like "$mes%"
                }


        val totalJornadas =
            jornadas.count()


        val jornadasAutomaticas =
            jornadas.count {
                it[JornadasLaboralesTable.cerradaAutomaticamente]
            }.toLong()


        val jornadasCorregidas =
            jornadas.count {
                it[JornadasLaboralesTable.corregidaPor] != null
            }.toLong()


        val totalTiempoLegal =
            jornadas.sumOf {
                it[JornadasLaboralesTable.tiempoLegal]
            }


        val totalTiempoExtra =
            jornadas.sumOf {
                it[JornadasLaboralesTable.tiempoExtraDetectado]
            }


        val totalTrabajadores =

            jornadas
                .map {
                    it[JornadasLaboralesTable.userId]
                }
                .distinct()
                .count()
                .toLong()


        JornadaResumenGlobalResponse(

            mes = mes,

            totalJornadas = totalJornadas,

            jornadasAutomaticas = jornadasAutomaticas,

            jornadasCorregidas = jornadasCorregidas,

            totalTiempoLegal = totalTiempoLegal,

            totalTiempoExtra = totalTiempoExtra,

            totalTrabajadores = totalTrabajadores
        )
    }

    fun obtenerJornadasPendientesRevision():
            List<JornadaPendienteRevisionResponse> = transaction {

        JornadasLaboralesTable
            .selectAll()
            .where {

                (JornadasLaboralesTable.cerradaAutomaticamente eq true) and

                        (JornadasLaboralesTable.corregidaPor.isNull())

            }
            .orderBy(
                JornadasLaboralesTable.fecha to SortOrder.ASC
            )
            .map {

                JornadaPendienteRevisionResponse(

                    id = it[JornadasLaboralesTable.id],

                    userId = it[JornadasLaboralesTable.userId],

                    username = it[UsuariosTable.username],

                    fecha = it[JornadasLaboralesTable.fecha],

                    entradaReal =
                        it[JornadasLaboralesTable.entradaReal],

                    salidaReal =
                        it[JornadasLaboralesTable.salidaReal],

                    cerradaAutomaticamente =
                        it[JornadasLaboralesTable.cerradaAutomaticamente],

                    tiempoExtraDetectado =
                        it[JornadasLaboralesTable.tiempoExtraDetectado]
                )
            }
    }

    fun corregirJornada(
        jornadaId: Int,
        nuevaSalidaReal: Long,
        adminId: Int,
        comentario: String?
    ) = transaction {

        val jornada =

            JornadasLaboralesTable
                .selectAll()
                .where {
                    JornadasLaboralesTable.id eq jornadaId
                }
                .singleOrNull()

                ?: error("Jornada no encontrada")


        val entradaReal =
            jornada[JornadasLaboralesTable.entradaReal]
                ?: error("Entrada real no válida")


        val fechaStr =
            jornada[JornadasLaboralesTable.fecha]


        val userId =
            jornada[JornadasLaboralesTable.userId]


        val zona =
            ZoneId.systemDefault()


        val fecha =
            LocalDate.parse(fechaStr)


        val inicioLegalEmpresa =
            fecha.atTime(
                EmpresaConfig.HORA_INICIO_JORNADA,
                EmpresaConfig.MINUTO_INICIO_JORNADA
            )
                .atZone(zona)
                .toInstant()
                .toEpochMilli()


        val finLegalEmpresa =
            fecha.atTime(
                EmpresaConfig.HORA_INICIO_JORNADA,
                EmpresaConfig.MINUTO_INICIO_JORNADA
            )
                .plusHours(
                    EmpresaConfig.DURACION_JORNADA_HORAS
                )
                .atZone(zona)
                .toInstant()
                .toEpochMilli()


        val entradaLegal =
            maxOf(entradaReal, inicioLegalEmpresa)


        val salidaLegal =
            minOf(nuevaSalidaReal, finLegalEmpresa)


        val tiempoLegal =
            maxOf(
                0L,
                salidaLegal - entradaLegal
            )


        val tiempoExtraDetectado =
            if (nuevaSalidaReal > finLegalEmpresa)
                nuevaSalidaReal - finLegalEmpresa
            else
                0L


        JornadasLaboralesTable.update({

            JornadasLaboralesTable.id eq jornadaId

        }) {

            it[salidaReal] = nuevaSalidaReal

            it[JornadasLaboralesTable.salidaLegal] =
                salidaLegal

            it[JornadasLaboralesTable.tiempoLegal] =
                tiempoLegal

            it[JornadasLaboralesTable.tiempoExtraDetectado] =
                tiempoExtraDetectado

            it[cerradaAutomaticamente] = false

            it[corregidaPor] = adminId

            it[comentarioAdmin] = comentario

            it[fechaCorreccion] =
                System.currentTimeMillis()
        }


        HorasExtrasTable.deleteWhere {

            (HorasExtrasTable.userId eq userId) and
                    (HorasExtrasTable.fecha eq fechaStr)

        }


        val minutosExtra =
            tiempoExtraDetectado / 60000


        if (minutosExtra > 0) {

            HorasExtrasTable.insert {

                it[HorasExtrasTable.userId] =
                    userId

                it[HorasExtrasTable.fecha] =
                    fechaStr

                it[HorasExtrasTable.minutosExtra] =
                    minutosExtra

                it[HorasExtrasTable.estado] =
                    "pendiente"
            }
        }
    }

    fun obtenerIncidencias(): List<JornadaIncidenciaResponse> = transaction {

        val incidencias =
            mutableListOf<JornadaIncidenciaResponse>()


        JornadasLaboralesTable
            .selectAll()
            .forEach {

                val jornadaId =
                    it[JornadasLaboralesTable.id]

                val userId =
                    it[JornadasLaboralesTable.userId]

                val fecha =
                    it[JornadasLaboralesTable.fecha]


                val tiempoLegal =
                    it[JornadasLaboralesTable.tiempoLegal]

                val descanso =
                    it[JornadasLaboralesTable.tiempoDescansoReal]


                if (it[JornadasLaboralesTable.cerradaAutomaticamente]) {

                    incidencias +=
                        JornadaIncidenciaResponse(
                            jornadaId,
                            userId,
                            fecha,
                            "CIERRE_AUTOMATICO"
                        )
                }


                if (tiempoLegal >
                    EmpresaConfig.DURACION_JORNADA_HORAS * 3600000
                ) {

                    incidencias +=
                        JornadaIncidenciaResponse(
                            jornadaId,
                            userId,
                            fecha,
                            "EXCESO_HORAS"
                        )
                }


                if (descanso < EmpresaConfig.DESCANSO_MINIMO_MS) {

                    incidencias +=
                        JornadaIncidenciaResponse(
                            jornadaId,
                            userId,
                            fecha,
                            "SIN_DESCANSO"
                        )
                }


                if (it[JornadasLaboralesTable.corregidaPor] != null) {

                    incidencias +=
                        JornadaIncidenciaResponse(
                            jornadaId,
                            userId,
                            fecha,
                            "CORREGIDA_ADMIN"
                        )
                }
            }


        incidencias
    }
}