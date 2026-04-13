package com.empresa.fichaje.services

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

    fun crearFichajeEvento(request: FichajeEventoRequest): Int {

        val estadoActual =
            obtenerEstadoActual(request.userId)

        validarTransicionEstado(
            estadoActual,
            request.contexto.name,
            request.accion.name
        )

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
        contexto: String,
        accion: String
    ): EstadoLaboral {

        return when {

            accion == "ENTRADA" && contexto == "TALLER" ->
                EstadoLaboral.EN_TALLER


            accion == "ENTRADA" && contexto == "OBRA" ->
                EstadoLaboral.EN_OBRA


            accion == "ENTRADA" && contexto == "REPARACION" ->
                EstadoLaboral.EN_REPARACION


            accion == "INICIO_DESCANSO" ->
                EstadoLaboral.EN_DESCANSO


            accion == "INICIO_VIAJE" && contexto == "OBRA" ->
                EstadoLaboral.VIAJANDO_A_OBRA


            accion == "INICIO_VIAJE" && contexto == "REPARACION" ->
                EstadoLaboral.VIAJANDO_A_REPARACION


            accion == "INICIO_VIAJE" && contexto == "TALLER" ->
                EstadoLaboral.VIAJANDO_A_TALLER


            // 👇 CAMBIO IMPORTANTE AQUÍ

            accion == "FIN_VIAJE" && contexto == "OBRA" ->
                EstadoLaboral.LLEGADO_OBRA


            accion == "FIN_VIAJE" && contexto == "REPARACION" ->
                EstadoLaboral.LLEGADO_REPARACION


            accion == "FIN_VIAJE" && contexto == "TALLER" ->
                EstadoLaboral.EN_TALLER


            accion == "SALIDA" ->
                EstadoLaboral.FUERA


            else ->
                EstadoLaboral.FUERA
        }
    }

    fun obtenerEstadoActual(userId: Int): EstadoLaboral {

        val ultimo = obtenerUltimoEventoRaw(userId)
            ?: return EstadoLaboral.FUERA

        return calcularEstadoActual(
            ultimo.first,
            ultimo.second
        )
    }

    fun validarTransicionEstado(
        estadoActual: EstadoLaboral,
        nuevoContexto: String,
        nuevaAccion: String
    ) {

        when (estadoActual) {

            EstadoLaboral.FUERA -> {

                if (
                    !(nuevoContexto == "TALLER"
                            && nuevaAccion == "ENTRADA")
                ) {

                    throw Exception(
                        "Debes iniciar la jornada entrando en TALLER"
                    )
                }
            }


            EstadoLaboral.EN_TALLER -> {

                if (
                    nuevaAccion == "INICIO_VIAJE"
                    && nuevoContexto == "TALLER"
                ) {

                    throw Exception(
                        "No puedes iniciar viaje hacia TALLER desde TALLER"
                    )
                }

                if (
                    nuevaAccion !in listOf(
                        "INICIO_DESCANSO",
                        "SALIDA",
                        "INICIO_VIAJE"
                    )
                ) {

                    throw Exception(
                        "Acción no válida desde TALLER"
                    )
                }
            }


            EstadoLaboral.VIAJANDO_A_OBRA -> {

                if (nuevaAccion != "FIN_VIAJE") {

                    throw Exception(
                        "Debes finalizar viaje antes de otra acción"
                    )
                }
            }


            EstadoLaboral.VIAJANDO_A_REPARACION -> {

                if (nuevaAccion != "FIN_VIAJE") {

                    throw Exception(
                        "Debes finalizar viaje antes de otra acción"
                    )
                }
            }


            EstadoLaboral.LLEGADO_OBRA -> {

                if (
                    nuevaAccion != "ENTRADA"
                    || nuevoContexto != "OBRA"
                ) {

                    throw Exception(
                        "Debes registrar ENTRADA_OBRA tras llegar a la obra"
                    )
                }
            }


            EstadoLaboral.LLEGADO_REPARACION -> {

                if (
                    nuevaAccion != "ENTRADA"
                    || nuevoContexto != "REPARACION"
                ) {

                    throw Exception(
                        "Debes registrar ENTRADA_REPARACION tras llegar"
                    )
                }
            }


            EstadoLaboral.EN_OBRA -> {

                if (
                    nuevaAccion == "INICIO_VIAJE"
                    && nuevoContexto != "TALLER"
                ) {

                    throw Exception(
                        "Desde OBRA solo puedes iniciar viaje hacia TALLER"
                    )
                }

                if (
                    nuevaAccion !in listOf(
                        "INICIO_DESCANSO",
                        "SALIDA",
                        "INICIO_VIAJE"
                    )
                ) {

                    throw Exception(
                        "Acción inválida en OBRA"
                    )
                }
            }


            EstadoLaboral.EN_REPARACION -> {

                if (
                    nuevaAccion == "INICIO_VIAJE"
                    && nuevoContexto != "TALLER"
                ) {

                    throw Exception(
                        "Desde REPARACION solo puedes iniciar viaje hacia TALLER"
                    )
                }

                if (
                    nuevaAccion !in listOf(
                        "INICIO_DESCANSO",
                        "SALIDA",
                        "INICIO_VIAJE"
                    )
                ) {

                    throw Exception(
                        "Acción inválida en REPARACION"
                    )
                }
            }


            EstadoLaboral.EN_DESCANSO -> {

                if (nuevaAccion != "FIN_DESCANSO") {

                    throw Exception(
                        "Debes finalizar descanso antes de continuar"
                    )
                }
            }


            EstadoLaboral.VIAJANDO_A_TALLER -> {

                if (nuevaAccion != "FIN_VIAJE") {

                    throw Exception(
                        "Debes finalizar viaje antes de continuar"
                    )
                }
            }
        }
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
    fun obtenerEstadoActualComoTexto(userId: Int): String {

        return obtenerEstadoActual(userId).name
    }

    fun obtenerEstadoDetallado(userId: Int): EstadoActualResponse {

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

        if (ultimoEvento == null) {

            return EstadoActualResponse(

                estado = EstadoLaboral.FUERA.name,

                contexto = null,

                accion = null,

                timestamp = null
            )
        }

        val contexto =
            ultimoEvento[FichajesEventosTable.contexto]

        val accion =
            ultimoEvento[FichajesEventosTable.accion]

        val timestamp =
            ultimoEvento[FichajesEventosTable.timestamp]


        val estado =
            calcularEstadoActual(
                contexto,
                accion
            ).name


        return EstadoActualResponse(

            estado = estado,

            contexto = contexto,

            accion = accion,

            timestamp = timestamp
        )
    }

    fun obtenerAccionesPermitidas(
        userId: Int
    ): SiguientesAccionesResponse {

        val estadoActual =
            obtenerEstadoActual(userId)


        val accionesPermitidas = when (estadoActual) {

            EstadoLaboral.FUERA -> listOf(
                "ENTRADA_TALLER"
            )


            EstadoLaboral.EN_TALLER -> listOf(
                "INICIO_DESCANSO",
                "SALIDA_TALLER",
                "INICIO_VIAJE_OBRA",
                "INICIO_VIAJE_REPARACION"
            )


            EstadoLaboral.VIAJANDO_A_OBRA -> listOf(
                "FIN_VIAJE_OBRA"
            )


            EstadoLaboral.VIAJANDO_A_REPARACION -> listOf(
                "FIN_VIAJE_REPARACION"
            )


            EstadoLaboral.LLEGADO_OBRA -> listOf(
                "ENTRADA_OBRA"
            )


            EstadoLaboral.LLEGADO_REPARACION -> listOf(
                "ENTRADA_REPARACION"
            )


            EstadoLaboral.EN_OBRA -> listOf(
                "INICIO_DESCANSO",
                "SALIDA_OBRA",
                "INICIO_VIAJE_TALLER"
            )


            EstadoLaboral.EN_REPARACION -> listOf(
                "INICIO_DESCANSO",
                "SALIDA_REPARACION",
                "INICIO_VIAJE_TALLER"
            )


            EstadoLaboral.EN_DESCANSO -> listOf(
                "FIN_DESCANSO"
            )


            EstadoLaboral.VIAJANDO_A_TALLER -> listOf(
                "FIN_VIAJE_TALLER"
            )
        }


        return SiguientesAccionesResponse(

            estado = estadoActual.name,

            accionesPermitidas = accionesPermitidas
        )
    }
}