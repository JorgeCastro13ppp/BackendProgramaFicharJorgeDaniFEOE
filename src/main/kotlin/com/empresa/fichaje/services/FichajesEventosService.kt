package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.domain.enums.*
import com.empresa.fichaje.domain.state.calcularEstadoActual
import com.empresa.fichaje.domain.state.validarCambioContexto
import com.empresa.fichaje.domain.state.validarTransicionEstado
import com.empresa.fichaje.dto.request.FichajeEventoRequest
import com.empresa.fichaje.dto.response.EstadoActualResponse
import com.empresa.fichaje.dto.response.FichajeResponse
import com.empresa.fichaje.dto.response.SiguientesAccionesResponse
import com.empresa.fichaje.utils.eventsBetweenDatesByUserOrdered
import com.empresa.fichaje.utils.latestFichajeResponse
import com.empresa.fichaje.utils.selectWhere
import com.empresa.fichaje.utils.todayRange
import com.empresa.fichaje.utils.toFichajeResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FichajesEventosService {

    fun crearFichajeEvento(
        request: FichajeEventoRequest
    ): Int {

        val estadoActual =
            obtenerEstadoActual(request.userId)

        validarTransicionEstado(
            estadoActual,
            request.accion.name
        )

        validarCambioContexto(
            estadoActual,
            request.contexto.name
        )

        return transaction {

            val insertedId =
                FichajesEventosTable.insert {

                    it[userId] = request.userId
                    it[timestamp] = request.timestamp
                    it[contexto] = request.contexto.name
                    it[accion] = request.accion.name
                    it[latitud] = request.latitud
                    it[longitud] = request.longitud
                    it[accuracy] = request.accuracy
                }[FichajesEventosTable.id]


            if (request.accion.name == "SALIDA") {

                HorasService()
                    .calcularJornadaLegal(
                        request.userId,
                        request.timestamp
                    )
            }

            insertedId
        }
    }


    fun obtenerFichajesParaAdmin(
        userId: Int? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<FichajeResponse> {

        return transaction {

            var query =
                (FichajesEventosTable innerJoin UsuariosTable)
                    .selectAll()

            if (userId != null) {

                query = query.andWhere {
                    FichajesEventosTable.userId eq userId
                }
            }

            val sortColumn = when (sortBy) {

                "usuario" -> UsuariosTable.username
                "accion" -> FichajesEventosTable.accion
                "contexto" -> FichajesEventosTable.contexto
                "id" -> FichajesEventosTable.id

                else -> FichajesEventosTable.timestamp
            }

            val sortOrder =
                if (order == "asc")
                    SortOrder.ASC
                else
                    SortOrder.DESC

            query
                .orderBy(sortColumn to sortOrder)
                .map { it.toFichajeResponse() }
        }
    }


    fun eliminarEvento(id: Int): Boolean {

        return transaction {

            FichajesEventosTable.deleteWhere {
                FichajesEventosTable.id eq id
            } > 0
        }
    }


    fun contarFichajesHoy(): Long {

        val (inicioDia, finDia) = todayRange()

        return transaction {

            FichajesEventosTable
                .selectWhere {

                    (FichajesEventosTable.timestamp greaterEq inicioDia) and
                            (FichajesEventosTable.timestamp less finDia)
                }
                .count()
        }
    }


    fun resumenFichajesHoy(): Map<String, Int> {

        val (inicioDia, finDia) = todayRange()

        return transaction {

            val eventosHoy =
                FichajesEventosTable.selectWhere {

                    (FichajesEventosTable.timestamp greaterEq inicioDia) and
                            (FichajesEventosTable.timestamp less finDia)
                }

            mapOf(

                "entradas" to eventosHoy.count {

                    AccionFichaje.valueOf(
                        it[FichajesEventosTable.accion]
                    ) == AccionFichaje.ENTRADA
                },

                "salidas" to eventosHoy.count {

                    AccionFichaje.valueOf(
                        it[FichajesEventosTable.accion]
                    ) == AccionFichaje.SALIDA
                },

                "viajes" to eventosHoy.count {

                    AccionFichaje.valueOf(
                        it[FichajesEventosTable.accion]
                    ).name.contains("VIAJE")
                },

                "descansos" to eventosHoy.count {

                    AccionFichaje.valueOf(
                        it[FichajesEventosTable.accion]
                    ).name.contains("DESCANSO")
                }
            )
        }
    }


    fun obtenerUltimoEvento(userId: Int): FichajeResponse? {

        return transaction {

            (FichajesEventosTable innerJoin UsuariosTable)
                .latestFichajeResponse(userId)
        }
    }


    fun obtenerEventosHoy(userId: Int): List<FichajeResponse> {

        val (inicioDia, finDia) = todayRange()

        return transaction {

            (FichajesEventosTable innerJoin UsuariosTable)
                .eventsBetweenDatesByUserOrdered(
                    userId,
                    inicioDia,
                    finDia
                )
                .map { it.toFichajeResponse() }
        }
    }


    fun obtenerEstadoActual(userId: Int): EstadoLaboral {

        val eventos = transaction {

            FichajesEventosTable
                .selectWhere {

                    FichajesEventosTable.userId eq userId
                }
                .orderBy(
                    FichajesEventosTable.timestamp,
                    SortOrder.ASC
                )
                .map {

                    ContextoFichaje.valueOf(
                        it[FichajesEventosTable.contexto]
                    ) to AccionFichaje.valueOf(
                        it[FichajesEventosTable.accion]
                    )
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
                    contexto.name,
                    accion.name
                )
        }

        return estadoActual
    }


    fun obtenerEstadoDetallado(
        userId: Int
    ): EstadoActualResponse {

        val ultimoEvento =
            obtenerUltimoEvento(userId)

        val estadoActual =
            obtenerEstadoActual(userId)

        return ultimoEvento?.let {

            val (accion, contexto) =
                it.tipo.split(" · ")

            EstadoActualResponse(
                estado = estadoActual.name,
                contexto = contexto,
                accion = accion,
                timestamp = it.fechaHora
            )

        } ?: EstadoActualResponse(
            estado = estadoActual.name,
            contexto = null,
            accion = null,
            timestamp = null
        )
    }


    fun obtenerAccionesPermitidas(
        userId: Int
    ): SiguientesAccionesResponse {

        val estado =
            obtenerEstadoActual(userId)

        val accionesTaller =
            mutableListOf<AccionPermitida>()

        val accionesObra =
            mutableListOf<AccionPermitida>()

        val accionesReparacion =
            mutableListOf<AccionPermitida>()


        when (estado) {

            EstadoLaboral.FUERA ->
                accionesTaller += AccionPermitida.ENTRADA_TALLER

            EstadoLaboral.EN_TALLER -> {

                accionesTaller += listOf(
                    AccionPermitida.INICIO_DESCANSO_TALLER,
                    AccionPermitida.SALIDA_TALLER
                )

                accionesObra +=
                    AccionPermitida.INICIO_VIAJE_OBRA

                accionesReparacion +=
                    AccionPermitida.INICIO_VIAJE_REPARACION
            }

            EstadoLaboral.DESCANSO_TALLER ->
                accionesTaller +=
                    AccionPermitida.FIN_DESCANSO_TALLER

            EstadoLaboral.VIAJE_IDA_OBRA ->
                accionesObra +=
                    AccionPermitida.FIN_VIAJE_OBRA

            EstadoLaboral.ESPERANDO_ENTRADA_OBRA ->
                accionesObra +=
                    AccionPermitida.ENTRADA_OBRA

            EstadoLaboral.EN_OBRA ->
                accionesObra += listOf(
                    AccionPermitida.INICIO_DESCANSO_OBRA,
                    AccionPermitida.SALIDA_OBRA
                )

            EstadoLaboral.DESCANSO_OBRA ->
                accionesObra +=
                    AccionPermitida.FIN_DESCANSO_OBRA

            EstadoLaboral.FIN_JORNADA_OBRA -> {

                accionesObra +=
                    AccionPermitida.ENTRADA_OBRA

                accionesTaller +=
                    AccionPermitida.INICIO_VIAJE_TALLER
            }

            EstadoLaboral.VIAJE_VUELTA_TALLER ->
                accionesTaller +=
                    AccionPermitida.FIN_VIAJE_TALLER

            EstadoLaboral.VIAJE_IDA_REPARACION ->
                accionesReparacion +=
                    AccionPermitida.FIN_VIAJE_REPARACION

            EstadoLaboral.ESPERANDO_ENTRADA_REPARACION ->
                accionesReparacion +=
                    AccionPermitida.ENTRADA_REPARACION

            EstadoLaboral.EN_REPARACION ->
                accionesReparacion += listOf(
                    AccionPermitida.INICIO_DESCANSO_REPARACION,
                    AccionPermitida.SALIDA_REPARACION
                )

            EstadoLaboral.DESCANSO_REPARACION ->
                accionesReparacion +=
                    AccionPermitida.FIN_DESCANSO_REPARACION

            EstadoLaboral.FIN_JORNADA_REPARACION -> {

                accionesReparacion +=
                    AccionPermitida.ENTRADA_REPARACION

                accionesTaller +=
                    AccionPermitida.INICIO_VIAJE_TALLER
            }
        }

        return SiguientesAccionesResponse(
            estado.name,
            accionesTaller,
            accionesObra,
            accionesReparacion
        )
    }

    fun formatearTiempo(ms: Long): String {

        val minutos = ms / 60000
        val horas = minutos / 60
        val restoMin = minutos % 60

        return "${horas}h ${restoMin}min"
    }
}