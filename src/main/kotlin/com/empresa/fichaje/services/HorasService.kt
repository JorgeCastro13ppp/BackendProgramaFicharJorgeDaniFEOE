package com.empresa.fichaje.services

import com.empresa.fichaje.database.FichajesEventosTable
import com.empresa.fichaje.models.AccionFichaje
import com.empresa.fichaje.models.HorasDiaResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class HorasService {

    fun calcularHoras(
        userId: Int,
        fechaInicio: Long,
        fechaFin: Long
    ): HorasDiaResponse {

        return transaction {

            val eventos = FichajesEventosTable
                .select {
                    (FichajesEventosTable.userId eq userId) and
                            (FichajesEventosTable.timestamp greaterEq fechaInicio) and
                            (FichajesEventosTable.timestamp lessEq fechaFin)
                }
                .orderBy(FichajesEventosTable.timestamp, SortOrder.ASC)
                .toList()

            if (eventos.isEmpty()) {
                return@transaction HorasDiaResponse(0, 0, 0, 0)
            }

            var tiempoTrabajo = 0L
            var tiempoViaje = 0L
            var tiempoDescanso = 0L

            var inicioTrabajo: Long? = null
            var inicioViaje: Long? = null
            var inicioDescanso: Long? = null

            eventos.forEach {

                val timestamp = it[FichajesEventosTable.timestamp]
                val accion = it[FichajesEventosTable.accion]

                when (accion) {

                    AccionFichaje.ENTRADA.name ->
                        inicioTrabajo = timestamp

                    AccionFichaje.SALIDA.name -> {
                        inicioTrabajo?.let {
                            tiempoTrabajo += timestamp - it
                            inicioTrabajo = null
                        }
                    }

                    AccionFichaje.INICIO_VIAJE.name ->
                        inicioViaje = timestamp

                    AccionFichaje.FIN_VIAJE.name -> {
                        inicioViaje?.let {
                            tiempoViaje += timestamp - it
                            inicioViaje = null
                        }
                    }

                    AccionFichaje.INICIO_DESCANSO.name ->
                        inicioDescanso = timestamp

                    AccionFichaje.FIN_DESCANSO.name -> {
                        inicioDescanso?.let {
                            tiempoDescanso += timestamp - it
                            inicioDescanso = null
                        }
                    }
                }
            }

            val tiempoTotal =
                eventos.last()[FichajesEventosTable.timestamp] -
                        eventos.first()[FichajesEventosTable.timestamp]

            HorasDiaResponse(
                tiempoTotal = tiempoTotal,
                tiempoTrabajo = tiempoTrabajo,
                tiempoViaje = tiempoViaje,
                tiempoDescanso = tiempoDescanso
            )
        }
    }
}