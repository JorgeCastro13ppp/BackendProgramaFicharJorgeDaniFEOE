package com.empresa.fichaje.background

import com.empresa.fichaje.config.EmpresaConfig
import com.empresa.fichaje.services.HorasService
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object BackgroundTasks {

    private val scope = CoroutineScope(
        Dispatchers.Default + SupervisorJob()
    )

    /*
    ========================
    CONTROL DOBLE EJECUCIÓN
    ========================
    */

    private var ultimaFechaProcesada: LocalDate? = null


    /*
    ========================
    START
    ========================
    */

    fun iniciar() {

        scope.launch {

            println(
                "BackgroundTasks iniciado"
            )

            while (true) {

                try {

                    val zona =
                        ZoneId.systemDefault()

                    val ahora =
                        LocalDateTime.now(zona)

                    val hoy =
                        LocalDate.now(zona)

                    /*
                    ========================
                    00:05
                    ========================
                    */

                    val esHoraCierre =

                        ahora.hour ==
                                EmpresaConfig.HORA_CIERRE_AUTOMATICO &&

                                ahora.minute >=
                                EmpresaConfig.MINUTO_CIERRE_AUTOMATICO


                    /*
                    ========================
                    EVITAR DUPLICADOS
                    ========================
                    */

                    val yaProcesadoHoy =
                        ultimaFechaProcesada == hoy


                    if (
                        esHoraCierre &&
                        !yaProcesadoHoy
                    ) {

                        val ayer =
                            hoy.minusDays(1)

                        println(
                            "Cerrando jornadas automáticamente: $ayer"
                        )

                        HorasService()
                            .cerrarJornadasPorFecha(
                                ayer.toString()
                            )

                        ultimaFechaProcesada =
                            hoy

                        println(
                            "Cierre automático completado"
                        )
                    }

                } catch (e: Exception) {

                    println(
                        "Error en scheduler de jornadas"
                    )

                    e.printStackTrace()
                }

                /*
                ========================
                CHECK CADA 30 SEG
                ========================
                */

                delay(
                    EmpresaConfig
                        .INTERVALO_REVISION_SCHEDULER_MS
                )
            }
        }
    }
}