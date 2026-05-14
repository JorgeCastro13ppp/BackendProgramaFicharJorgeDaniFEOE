package com.empresa.fichaje.config

object EmpresaConfig {

    /*
    ========================
    JORNADA LABORAL
    ========================
    */

    const val HORA_INICIO_JORNADA = 7

    const val MINUTO_INICIO_JORNADA = 0

    const val DURACION_JORNADA_HORAS = 8L


    /*
    ========================
    DESCANSOS
    ========================
    */

    const val DESCANSO_MINIMO_MS =
        30 * 60 * 1000L


    /*
    ========================
    HORAS EXTRA
    ========================
    */

    const val MINUTOS_MINIMOS_EXTRA = 15


    /*
    ========================
    CIERRE AUTOMÁTICO
    ========================
    */

    const val HORA_CIERRE_AUTOMATICO = 0

    const val MINUTO_CIERRE_AUTOMATICO = 5


    /*
    ========================
    SCHEDULER
    ========================
    */

    const val INTERVALO_REVISION_SCHEDULER_MS =
        30_000L
}