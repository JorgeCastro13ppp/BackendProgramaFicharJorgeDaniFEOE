package com.empresa.fichaje.domain.models

import com.empresa.fichaje.domain.enums.AccionFichaje
import com.empresa.fichaje.domain.enums.ContextoFichaje

data class TimelineEvent(

    val accion: AccionFichaje,

    val contexto: ContextoFichaje,

    val timestamp: Long
)


data class UserDailyTimeline(

    val userId: Int,

    val eventos: List<TimelineEvent>
) {

    fun totalTrabajo(now: Long): Long {

        var inicioTrabajo: Long? = null

        var enDescanso = false
        var enViaje = false

        var total = 0L

        eventos.forEachIndexed { index, evento ->

            when (evento.accion) {

                AccionFichaje.ENTRADA -> {

                    if (!enDescanso && !enViaje) {
                        inicioTrabajo = evento.timestamp
                    }
                }

                AccionFichaje.SALIDA -> {

                    inicioTrabajo?.let {
                        total += evento.timestamp - it
                    }

                    /*
                    ========================
                    ¿CONTINÚA EN TALLER?
                    ========================
                    */

                    val siguienteEvento =
                        eventos.getOrNull(index + 1)

                    val continuaJornada =
                        siguienteEvento?.accion == AccionFichaje.INICIO_VIAJE &&
                                siguienteEvento.contexto == ContextoFichaje.TALLER

                    inicioTrabajo =
                        if (continuaJornada)
                            evento.timestamp
                        else
                            null
                }

                AccionFichaje.INICIO_DESCANSO -> {

                    inicioTrabajo?.let {
                        total += evento.timestamp - it
                        inicioTrabajo = null
                    }

                    enDescanso = true
                }

                AccionFichaje.FIN_DESCANSO -> {

                    enDescanso = false
                    inicioTrabajo = evento.timestamp
                }

                AccionFichaje.INICIO_VIAJE -> {

                    inicioTrabajo?.let {
                        total += evento.timestamp - it
                        inicioTrabajo = null
                    }

                    enViaje = true
                }

                AccionFichaje.FIN_VIAJE -> {

                    enViaje = false
                    inicioTrabajo = evento.timestamp
                }

                else -> {}
            }
        }

        /*
        ========================
        JORNADA ABIERTA
        ========================
        */

        inicioTrabajo?.let {

            total += now - it
        }

        return total
    }

    fun tiempoTrabajoEfectivo(
        tiempoLegal: Long,
        tiempoExtra: Long
    ): Long {

        return maxOf(
            0L,
            tiempoLegal +
                    tiempoExtra -
                    totalDescanso() -
                    totalViaje()
        )
    }


    fun totalDescanso(): Long {

        var inicioDescanso: Long? = null
        var total = 0L

        eventos.forEach { evento ->

            when (evento.accion) {

                AccionFichaje.INICIO_DESCANSO -> {
                    inicioDescanso = evento.timestamp
                }

                AccionFichaje.FIN_DESCANSO -> {
                    inicioDescanso?.let {
                        total += evento.timestamp - it
                        inicioDescanso = null
                    }
                }

                else -> {}
            }
        }

        return total
    }


    fun totalViaje(): Long {

        var inicioViaje: Long? = null
        var total = 0L

        eventos.forEach { evento ->

            when (evento.accion) {

                AccionFichaje.INICIO_VIAJE -> {
                    inicioViaje = evento.timestamp
                }

                AccionFichaje.FIN_VIAJE -> {
                    inicioViaje?.let {
                        total += evento.timestamp - it
                        inicioViaje = null
                    }
                }

                else -> {}
            }
        }

        return total
    }


    fun tiempoTotalJornada(): Long {

        if (eventos.isEmpty()) return 0

        return eventos.last().timestamp - eventos.first().timestamp
    }

    fun firstEntrada(): Long? {

        return eventos
            .firstOrNull {
                it.accion == AccionFichaje.ENTRADA
            }
            ?.timestamp
    }


    fun lastSalidaFinal(): Long? {

        return eventos
            .filter {
                it.accion == AccionFichaje.SALIDA
            }
            .maxByOrNull {
                it.timestamp
            }
            ?.timestamp
    }
}