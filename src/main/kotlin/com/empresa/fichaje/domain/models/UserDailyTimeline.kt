package com.empresa.fichaje.domain.models

import com.empresa.fichaje.domain.enums.AccionFichaje

data class TimelineEvent(

    val accion: AccionFichaje,

    val timestamp: Long
)


data class UserDailyTimeline(

    val userId: Int,

    val eventos: List<TimelineEvent>
) {

    fun totalTrabajo(now: Long = System.currentTimeMillis()): Long {

        var inicioTrabajo: Long? = null
        var enDescanso = false
        var total = 0L

        eventos.forEach { evento ->

            when (evento.accion) {

                AccionFichaje.ENTRADA -> {
                    inicioTrabajo = evento.timestamp
                }

                AccionFichaje.SALIDA -> {
                    inicioTrabajo?.let {
                        total += evento.timestamp - it
                        inicioTrabajo = null
                    }
                }

                AccionFichaje.INICIO_DESCANSO -> {
                    inicioTrabajo?.let {
                        total += evento.timestamp - it
                        enDescanso = true
                    }
                }

                AccionFichaje.FIN_DESCANSO -> {
                    if (enDescanso) {
                        inicioTrabajo = evento.timestamp
                        enDescanso = false
                    }
                }

                else -> {}
            }
        }

        inicioTrabajo?.let {
            total += now - it
        }

        return total
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


    fun lastSalida(): Long? {

        return eventos
            .lastOrNull {
                it.accion == AccionFichaje.SALIDA
            }
            ?.timestamp
    }
}