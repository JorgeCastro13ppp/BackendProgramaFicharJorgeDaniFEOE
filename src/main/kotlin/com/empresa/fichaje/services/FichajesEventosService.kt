package com.empresa.fichaje.services

import com.empresa.fichaje.database.FichajesEventosTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.AccionFichaje
import com.empresa.fichaje.models.FichajeEventoRequest
import com.empresa.fichaje.models.FichajeResponse
import io.ktor.client.request.request
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class FichajesEventosService {

    fun crearFichajeEvento(request: FichajeEventoRequest): Int {

        return transaction {

            val ultimoEventoGlobal = obtenerUltimoEventoUsuario(request.userId)

            if (ultimoEventoGlobal != null) {

                validarSecuenciaGlobal(
                    userId = request.userId,
                    ultimaAccion = ultimoEventoGlobal[FichajesEventosTable.accion],
                    nuevoContexto = request.contexto.name,
                    nuevaAccion = request.accion
                )
            }

            val ultimoEventoMismoContexto =
                obtenerUltimoEventoMismoContexto(request.userId, request.contexto.name)

            if (ultimoEventoMismoContexto != null) {

                validarSecuenciaContexto(
                    ultimaAccion = ultimoEventoMismoContexto[FichajesEventosTable.accion],
                    nuevaAccion = request.accion
                )
            }

            FichajesEventosTable.insert {

                it[userId] = request.userId
                it[timestamp] = request.timestamp
                it[contexto] = request.contexto.name
                it[accion] = request.accion.name
                it[latitud] = request.latitud
                it[longitud] = request.longitud
                it[accuracy] = request.accuracy
            } get FichajesEventosTable.id
        }
    }


    private fun obtenerUltimoEventoUsuario(userId: Int) =
        FichajesEventosTable
            .select { FichajesEventosTable.userId eq userId }
            .orderBy(FichajesEventosTable.timestamp, SortOrder.DESC)
            .limit(1)
            .firstOrNull()


    private fun obtenerUltimoEventoMismoContexto(userId: Int, contexto: String) =
        FichajesEventosTable
            .select {
                (FichajesEventosTable.userId eq userId) and
                        (FichajesEventosTable.contexto eq contexto)
            }
            .orderBy(FichajesEventosTable.timestamp, SortOrder.DESC)
            .limit(1)
            .firstOrNull()


    private fun validarSecuenciaContexto(
        ultimaAccion: String,
        nuevaAccion: AccionFichaje
    ) {

        if (ultimaAccion == nuevaAccion.name) {
            throw IllegalArgumentException(
                "No puedes registrar dos acciones '${nuevaAccion.name}' seguidas"
            )
        }

        if (nuevaAccion == AccionFichaje.SALIDA &&
            ultimaAccion != AccionFichaje.ENTRADA.name &&
            ultimaAccion != AccionFichaje.FIN_DESCANSO.name &&
            ultimaAccion != AccionFichaje.FIN_VIAJE.name
        ) {
            throw IllegalArgumentException(
                "No puedes registrar SALIDA sin ENTRADA previa"
            )
        }

        if (nuevaAccion == AccionFichaje.FIN_VIAJE &&
            ultimaAccion != AccionFichaje.INICIO_VIAJE.name
        ) {
            throw IllegalArgumentException(
                "No puedes registrar FIN_VIAJE sin INICIO_VIAJE previo"
            )
        }

        if (nuevaAccion == AccionFichaje.FIN_DESCANSO &&
            ultimaAccion != AccionFichaje.INICIO_DESCANSO.name
        ) {
            throw IllegalArgumentException(
                "No puedes registrar FIN_DESCANSO sin INICIO_DESCANSO previo"
            )
        }
    }


    private fun validarSecuenciaGlobal(
        userId: Int,
        ultimaAccion: String,
        nuevoContexto: String,
        nuevaAccion: AccionFichaje
    ) {

        val sesionTrabajoAbierta =
            ultimaAccion == AccionFichaje.ENTRADA.name

        if (sesionTrabajoAbierta &&
            nuevaAccion == AccionFichaje.ENTRADA &&
            nuevoContexto != obtenerContextoUltimoEvento(userId)
        ) {
            throw IllegalArgumentException(
                "Debes cerrar la sesión actual antes de fichar en otro contexto"
            )
        }

        if (ultimaAccion == AccionFichaje.INICIO_DESCANSO.name &&
            nuevaAccion != AccionFichaje.FIN_DESCANSO
        ) {
            throw IllegalArgumentException(
                "Debes finalizar el descanso antes de realizar otra acción"
            )
        }

        if (ultimaAccion == AccionFichaje.INICIO_VIAJE.name &&
            nuevaAccion != AccionFichaje.FIN_VIAJE
        ) {
            throw IllegalArgumentException(
                "Debes finalizar el viaje antes de realizar otra acción"
            )
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


    private fun obtenerContextoUltimoEvento(userId: Int): String =
        FichajesEventosTable
            .select { FichajesEventosTable.userId eq userId }
            .orderBy(FichajesEventosTable.timestamp, SortOrder.DESC)
            .limit(1)
            .first()[FichajesEventosTable.contexto]
}