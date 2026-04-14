package com.empresa.fichaje.services

import EstadoLaboral
import com.empresa.fichaje.database.FichajesEventosTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.AccionFichaje
import com.empresa.fichaje.models.EstadoActualResponse
import com.empresa.fichaje.models.FichajeEventoRequest
import com.empresa.fichaje.models.FichajeResponse
import com.empresa.fichaje.models.SiguientesAccionesResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

import org.jetbrains.exposed.sql.insertAndGetId

class FichajesEventosService {

    fun crearFichajeEvento(
        request: FichajeEventoRequest
    ): Int {

        val estadoActual =
            obtenerEstadoActual(request.userId)


        // Validar transición de estado
        validarTransicionEstado(
            estadoActual,
            request.accion.name
        )


        // (Opcional) validar coherencia de contexto
        validarCambioContexto(
            estadoActual,
            request.contexto.name
        )


        return transaction {

            val insertStatement =
                FichajesEventosTable.insert {

                    it[userId] = request.userId
                    it[timestamp] = request.timestamp
                    it[contexto] = request.contexto.name
                    it[accion] = request.accion.name
                    it[latitud] = request.latitud
                    it[longitud] = request.longitud
                    it[accuracy] = request.accuracy
                }

            insertStatement[FichajesEventosTable.id]
        }
    }



    fun obtenerTodosParaAdmin(): List<FichajeResponse> {

        return transaction {

            (FichajesEventosTable innerJoin UsuariosTable)
                .selectAll()
                .orderBy(FichajesEventosTable.timestamp, SortOrder.DESC)
                .map {

                    FichajeResponse(
                        id = it[FichajesEventosTable.id],
                        userId = it[FichajesEventosTable.userId],
                        username = it[UsuariosTable.username],
                        fechaHora = it[FichajesEventosTable.timestamp],
                        tipo = "${it[FichajesEventosTable.accion]} · ${it[FichajesEventosTable.contexto]}".lowercase()
                    )
                }
        }
    }

    fun obtenerFichajesPorUsuarioParaAdmin(userId: Int): List<FichajeResponse> {

        return transaction {

            (FichajesEventosTable innerJoin UsuariosTable)
                .select {
                    FichajesEventosTable.userId eq userId
                }
                .orderBy(FichajesEventosTable.timestamp, SortOrder.DESC)
                .map {

                    FichajeResponse(
                        id = it[FichajesEventosTable.id],
                        userId = it[FichajesEventosTable.userId],
                        username = it[UsuariosTable.username],
                        fechaHora = it[FichajesEventosTable.timestamp],
                        tipo = "${it[FichajesEventosTable.accion]} · ${it[FichajesEventosTable.contexto]}".lowercase()
                    )
                }
        }
    }

    fun eliminarEvento(id: Int): Boolean {

        return transaction {

            FichajesEventosTable
                .deleteWhere {
                    FichajesEventosTable.id eq id
                } > 0
        }
    }

    fun contarFichajesHoy(): Long {

        val inicioDia = java.time.LocalDate
            .now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val finDia = java.time.LocalDate
            .now()
            .plusDays(1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return transaction {

            FichajesEventosTable
                .select {
                    (FichajesEventosTable.timestamp greaterEq inicioDia) and
                            (FichajesEventosTable.timestamp less finDia)
                }
                .count()
        }
    }

    fun resumenFichajesHoy(): Map<String, Int> {

        val inicioDia =
            java.time.LocalDate.now()
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        val finDia =
            java.time.LocalDate.now()
                .plusDays(1)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        return transaction {

            val eventosHoy =
                FichajesEventosTable
                    .select {
                        (FichajesEventosTable.timestamp greaterEq inicioDia) and
                                (FichajesEventosTable.timestamp less finDia)
                    }

            mapOf(
                "entradas" to eventosHoy.count {
                    it[FichajesEventosTable.accion] == "ENTRADA"
                },
                "salidas" to eventosHoy.count {
                    it[FichajesEventosTable.accion] == "SALIDA"
                },
                "viajes" to eventosHoy.count {
                    it[FichajesEventosTable.accion].contains("VIAJE")
                },
                "descansos" to eventosHoy.count {
                    it[FichajesEventosTable.accion].contains("DESCANSO")
                }
            )
        }
    }

    fun obtenerUltimoEvento(userId: Int): FichajeResponse? {

        return transaction {

            (FichajesEventosTable innerJoin UsuariosTable)
                .select {
                    FichajesEventosTable.userId eq userId
                }
                .orderBy(
                    FichajesEventosTable.timestamp,
                    SortOrder.DESC
                )
                .limit(1)
                .map {

                    FichajeResponse(
                        id = it[FichajesEventosTable.id],
                        userId = it[FichajesEventosTable.userId],
                        username = it[UsuariosTable.username],
                        fechaHora = it[FichajesEventosTable.timestamp],
                        tipo =
                            "${it[FichajesEventosTable.accion]} · ${it[FichajesEventosTable.contexto]}"
                                .lowercase()
                    )
                }
                .firstOrNull()
        }
    }

    fun obtenerEventosHoy(userId: Int): List<FichajeResponse> {

        return transaction {

            val inicioDia =
                java.time.LocalDate.now()
                    .atStartOfDay(
                        java.time.ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli()

            val finDia =
                java.time.LocalDate.now()
                    .plusDays(1)
                    .atStartOfDay(
                        java.time.ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli() - 1


            (FichajesEventosTable innerJoin UsuariosTable)
                .select {

                    (FichajesEventosTable.userId eq userId) and
                            (FichajesEventosTable.timestamp greaterEq inicioDia) and
                            (FichajesEventosTable.timestamp lessEq finDia)
                }
                .orderBy(
                    FichajesEventosTable.timestamp,
                    SortOrder.ASC
                )
                .map {

                    FichajeResponse(
                        id = it[FichajesEventosTable.id],
                        userId = it[FichajesEventosTable.userId],
                        username = it[UsuariosTable.username],
                        fechaHora = it[FichajesEventosTable.timestamp],
                        tipo =
                            "${it[FichajesEventosTable.accion]} · ${it[FichajesEventosTable.contexto]}"
                                .lowercase()
                    )
                }
        }
    }

    fun calcularEstadoActual(
        estadoAnterior: EstadoLaboral,
        contexto: String,
        accion: String
    ): EstadoLaboral {

        return when (estadoAnterior) {

            EstadoLaboral.FUERA -> {

                if (accion == "ENTRADA" && contexto == "TALLER")
                    EstadoLaboral.EN_TALLER
                else
                    EstadoLaboral.FUERA
            }


            EstadoLaboral.EN_TALLER -> when {

                accion == "INICIO_DESCANSO" ->
                    EstadoLaboral.DESCANSO_TALLER

                accion == "SALIDA" ->
                    EstadoLaboral.FUERA

                accion == "INICIO_VIAJE" && contexto == "OBRA" ->
                    EstadoLaboral.VIAJE_IDA_OBRA

                accion == "INICIO_VIAJE" && contexto == "REPARACION" ->
                    EstadoLaboral.VIAJE_IDA_REPARACION

                else -> estadoAnterior
            }


            EstadoLaboral.DESCANSO_TALLER -> {

                if (accion == "FIN_DESCANSO")
                    EstadoLaboral.EN_TALLER
                else
                    estadoAnterior
            }


            EstadoLaboral.VIAJE_IDA_OBRA -> {

                if (accion == "FIN_VIAJE")
                    EstadoLaboral.ESPERANDO_ENTRADA_OBRA
                else
                    estadoAnterior
            }


            EstadoLaboral.ESPERANDO_ENTRADA_OBRA -> {

                if (accion == "ENTRADA")
                    EstadoLaboral.EN_OBRA
                else
                    estadoAnterior
            }


            EstadoLaboral.EN_OBRA -> when {

                accion == "INICIO_DESCANSO" ->
                    EstadoLaboral.DESCANSO_OBRA

                accion == "SALIDA" ->
                    EstadoLaboral.FIN_JORNADA_OBRA

                else -> estadoAnterior
            }


            EstadoLaboral.DESCANSO_OBRA -> {

                if (accion == "FIN_DESCANSO")
                    EstadoLaboral.EN_OBRA
                else
                    estadoAnterior
            }


            EstadoLaboral.FIN_JORNADA_OBRA -> when {

                accion == "ENTRADA" ->
                    EstadoLaboral.EN_OBRA

                accion == "INICIO_VIAJE" && contexto == "TALLER" ->
                    EstadoLaboral.VIAJE_VUELTA_TALLER

                else -> estadoAnterior
            }


            EstadoLaboral.VIAJE_VUELTA_TALLER -> {

                if (accion == "FIN_VIAJE")
                    EstadoLaboral.EN_TALLER
                else
                    estadoAnterior
            }


            EstadoLaboral.VIAJE_IDA_REPARACION -> {

                if (accion == "FIN_VIAJE")
                    EstadoLaboral.ESPERANDO_ENTRADA_REPARACION
                else
                    estadoAnterior
            }


            EstadoLaboral.ESPERANDO_ENTRADA_REPARACION -> {

                if (accion == "ENTRADA")
                    EstadoLaboral.EN_REPARACION
                else
                    estadoAnterior
            }


            EstadoLaboral.EN_REPARACION -> when {

                accion == "INICIO_DESCANSO" ->
                    EstadoLaboral.DESCANSO_REPARACION

                accion == "SALIDA" ->
                    EstadoLaboral.FIN_JORNADA_REPARACION

                else -> estadoAnterior
            }


            EstadoLaboral.DESCANSO_REPARACION -> {

                if (accion == "FIN_DESCANSO")
                    EstadoLaboral.EN_REPARACION
                else
                    estadoAnterior
            }


            EstadoLaboral.FIN_JORNADA_REPARACION -> when {

                accion == "ENTRADA" ->
                    EstadoLaboral.EN_REPARACION

                accion == "INICIO_VIAJE" && contexto == "TALLER" ->
                    EstadoLaboral.VIAJE_VUELTA_TALLER

                else -> estadoAnterior
            }
        }
    }

    fun obtenerEstadoActual(
        userId: Int
    ): EstadoLaboral {

        val eventos = transaction {

            FichajesEventosTable
                .select {
                    FichajesEventosTable.userId eq userId
                }
                .orderBy(
                    FichajesEventosTable.timestamp,
                    SortOrder.ASC
                )
                .map {

                    it[FichajesEventosTable.contexto] to
                            it[FichajesEventosTable.accion]
                }
        }

        if (eventos.isEmpty())
            return EstadoLaboral.FUERA


        var estadoActual =
            EstadoLaboral.FUERA


        eventos.forEach { (contexto, accion) ->

            estadoActual =
                calcularEstadoActual(
                    estadoActual,
                    contexto,
                    accion
                )
        }

        return estadoActual
    }

    fun validarTransicionEstado(
        estadoActual: EstadoLaboral,
        accion: String
    ) {

        val permitidas = when (estadoActual) {

            EstadoLaboral.FUERA ->
                listOf("ENTRADA")

            EstadoLaboral.EN_TALLER ->
                listOf("INICIO_DESCANSO", "SALIDA", "INICIO_VIAJE")

            EstadoLaboral.DESCANSO_TALLER ->
                listOf("FIN_DESCANSO")

            EstadoLaboral.VIAJE_IDA_OBRA,
            EstadoLaboral.VIAJE_IDA_REPARACION,
            EstadoLaboral.VIAJE_VUELTA_TALLER ->
                listOf("FIN_VIAJE")

            EstadoLaboral.ESPERANDO_ENTRADA_OBRA,
            EstadoLaboral.ESPERANDO_ENTRADA_REPARACION ->
                listOf("ENTRADA")

            EstadoLaboral.EN_OBRA,
            EstadoLaboral.EN_REPARACION ->
                listOf("INICIO_DESCANSO", "SALIDA")

            EstadoLaboral.DESCANSO_OBRA,
            EstadoLaboral.DESCANSO_REPARACION ->
                listOf("FIN_DESCANSO")

            EstadoLaboral.FIN_JORNADA_OBRA,
            EstadoLaboral.FIN_JORNADA_REPARACION ->
                listOf("ENTRADA", "INICIO_VIAJE")
        }

        if (accion !in permitidas)
            throw Exception("Acción no permitida desde el estado actual")
    }


    fun validarCambioContexto(
        estadoActual: EstadoLaboral,
        nuevoContexto: String
    ) {

        when (estadoActual) {

            EstadoLaboral.EN_TALLER -> {

                if (nuevoContexto !in listOf("TALLER", "OBRA", "REPARACION")) {

                    throw Exception("Cambio de contexto inválido desde TALLER")
                }
            }

            EstadoLaboral.EN_OBRA -> {

                if (nuevoContexto !in listOf("OBRA", "TALLER")) {

                    throw Exception("Debes volver al TALLER antes de cambiar contexto")
                }
            }

            EstadoLaboral.EN_REPARACION -> {

                if (nuevoContexto !in listOf("REPARACION", "TALLER")) {

                    throw Exception("Debes volver al TALLER antes de cambiar contexto")
                }
            }

            else -> {}
        }
    }

    private fun obtenerUltimoEventoRaw(userId: Int): Pair<String, String>? {

        return transaction {

            FichajesEventosTable
                .select { FichajesEventosTable.userId eq userId }
                .orderBy(FichajesEventosTable.timestamp, SortOrder.DESC)
                .limit(1)
                .map {

                    it[FichajesEventosTable.contexto] to
                            it[FichajesEventosTable.accion]

                }
                .firstOrNull()
        }
    }

    fun obtenerEstadoDetallado(
        userId: Int
    ): EstadoActualResponse {

        val ultimoEvento = transaction {

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
        }


        val estadoActual =
            obtenerEstadoActual(userId)


        if (ultimoEvento == null) {

            return EstadoActualResponse(

                estado = estadoActual.name,

                contexto = null,

                accion = null,

                timestamp = null
            )
        }


        return EstadoActualResponse(

            estado = estadoActual.name,

            contexto =
                ultimoEvento[FichajesEventosTable.contexto],

            accion =
                ultimoEvento[FichajesEventosTable.accion],

            timestamp =
                ultimoEvento[FichajesEventosTable.timestamp]
        )
    }

    fun obtenerAccionesPermitidas(
        userId: Int
    ): SiguientesAccionesResponse {

        val estado = obtenerEstadoActual(userId)

        val accionesTaller = mutableListOf<String>()
        val accionesObra = mutableListOf<String>()
        val accionesReparacion = mutableListOf<String>()

        when (estado) {

            EstadoLaboral.FUERA -> {

                accionesTaller += "ENTRADA_TALLER"
            }

            EstadoLaboral.EN_TALLER -> {

                accionesTaller += listOf(
                    "INICIO_DESCANSO_TALLER",
                    "SALIDA_TALLER"
                )

                accionesObra += "INICIO_VIAJE_OBRA"
                accionesReparacion += "INICIO_VIAJE_REPARACION"
            }

            EstadoLaboral.DESCANSO_TALLER -> {

                accionesTaller += "FIN_DESCANSO_TALLER"
            }

            EstadoLaboral.VIAJE_IDA_OBRA -> {

                accionesObra += "FIN_VIAJE_OBRA"
            }

            EstadoLaboral.ESPERANDO_ENTRADA_OBRA -> {

                accionesObra += "ENTRADA_OBRA"
            }

            EstadoLaboral.EN_OBRA -> {

                accionesObra += listOf(
                    "INICIO_DESCANSO_OBRA",
                    "SALIDA_OBRA"
                )
            }

            EstadoLaboral.DESCANSO_OBRA -> {

                accionesObra += "FIN_DESCANSO_OBRA"
            }

            EstadoLaboral.FIN_JORNADA_OBRA -> {

                accionesObra += "ENTRADA_OBRA"
                accionesTaller += "INICIO_VIAJE_TALLER"
            }

            EstadoLaboral.VIAJE_VUELTA_TALLER -> {

                accionesTaller += "FIN_VIAJE_TALLER"
            }

            EstadoLaboral.VIAJE_IDA_REPARACION -> {

                accionesReparacion += "FIN_VIAJE_REPARACION"
            }

            EstadoLaboral.ESPERANDO_ENTRADA_REPARACION -> {

                accionesReparacion += "ENTRADA_REPARACION"
            }

            EstadoLaboral.EN_REPARACION -> {

                accionesReparacion += listOf(
                    "INICIO_DESCANSO_REPARACION",
                    "SALIDA_REPARACION"
                )
            }

            EstadoLaboral.DESCANSO_REPARACION -> {

                accionesReparacion += "FIN_DESCANSO_REPARACION"
            }

            EstadoLaboral.FIN_JORNADA_REPARACION -> {

                accionesReparacion += "ENTRADA_REPARACION"
                accionesTaller += "INICIO_VIAJE_TALLER"
            }
        }

        return SiguientesAccionesResponse(

            estado = estado.name,

            accionesTaller = accionesTaller,

            accionesObra = accionesObra,

            accionesReparacion = accionesReparacion
        )
    }

}