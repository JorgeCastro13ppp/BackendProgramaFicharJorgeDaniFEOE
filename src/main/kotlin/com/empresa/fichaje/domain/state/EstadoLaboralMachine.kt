package com.empresa.fichaje.domain.state

import com.empresa.fichaje.domain.enums.EstadoLaboral

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