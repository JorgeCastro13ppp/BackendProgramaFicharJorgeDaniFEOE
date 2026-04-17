package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.database.tables.VacacionesResumenTable
import com.empresa.fichaje.database.tables.VacacionesTable
import com.empresa.fichaje.domain.enums.EstadoVacaciones
import com.empresa.fichaje.domain.enums.Role
import com.empresa.fichaje.dto.response.VacacionesAlertaResponse
import com.empresa.fichaje.dto.response.VacacionesResponse
import com.empresa.fichaje.dto.response.VacacionesResumenAdminResponse
import com.empresa.fichaje.dto.response.VacacionesResumenResponse
import com.empresa.fichaje.utils.toEstadoVacacionesOrNull
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class VacacionesService {

    fun solicitar(
        userId: Int,
        fechaInicio: String,
        fechaFin: String
    ) = transaction {

        val inicio = LocalDate.parse(fechaInicio)
        val fin = LocalDate.parse(fechaFin)

        if (fin.isBefore(inicio)) {
            error("La fecha fin no puede ser anterior a la fecha inicio")
        }

        val existeSolapamiento =
            VacacionesTable
                .selectAll()
                .where {
                    (VacacionesTable.userId eq userId) and
                            (VacacionesTable.fechaInicio lessEq fechaFin) and
                            (VacacionesTable.fechaFin greaterEq fechaInicio)
                }
                .count() > 0

        if (existeSolapamiento) {
            error("El usuario ya tiene vacaciones en ese periodo")
        }

        val diasSolicitados =
            ChronoUnit.DAYS.between(inicio, fin).toInt() + 1

        val esNavidad =
            esPeriodoNavidad(inicio, fin)

        val resumen =
            obtenerResumen(userId, inicio.year)

        val usadosNavidad =
            resumen[VacacionesResumenTable.diasNavidadUsados]

        val usadosLibres =
            resumen[VacacionesResumenTable.diasLibresUsados]

        if (esNavidad) {

            val restantes =
                15 - usadosNavidad -
                        diasPendientesNavidad(userId)

            if (diasSolicitados > restantes) {
                error("No tienes suficientes días disponibles en Navidad")
            }

        } else {

            val restantes =
                15 - usadosLibres -
                        diasPendientesLibres(userId)

            if (diasSolicitados > restantes) {
                error("No tienes suficientes días libres disponibles")
            }
        }

        VacacionesTable.insert {

            it[VacacionesTable.userId] = userId
            it[VacacionesTable.fechaInicio] = fechaInicio
            it[VacacionesTable.fechaFin] = fechaFin
            it[VacacionesTable.estado] =
                EstadoVacaciones.PENDIENTE.name.lowercase()

            it[VacacionesTable.tipo] =
                if (esNavidad) "navidad" else "libre"
        }
    }


    fun obtener(
        userId: Int,
        role: Role,
        estado: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<VacacionesResponse> = transaction {

        val query =
            VacacionesTable
                .innerJoin(UsuariosTable)
                .selectAll()
                .apply {

                    if (role != Role.ADMIN) {
                        andWhere {
                            VacacionesTable.userId eq userId
                        }
                    }

                    estado?.let {
                        andWhere {
                            VacacionesTable.estado eq it
                        }
                    }
                }

        val sortColumn =
            when (sortBy) {
                "inicio" -> VacacionesTable.fechaInicio
                "fin" -> VacacionesTable.fechaFin
                "usuario" -> UsuariosTable.username
                "estado" -> VacacionesTable.estado
                else -> VacacionesTable.fechaInicio
            }

        val sortOrder =
            if (order == "desc") SortOrder.DESC
            else SortOrder.ASC

        query
            .orderBy(sortColumn to sortOrder)
            .map {

                VacacionesResponse(
                    id = it[VacacionesTable.id],
                    userId = it[VacacionesTable.userId],
                    username = it[UsuariosTable.username],
                    fechaInicio = it[VacacionesTable.fechaInicio],
                    fechaFin = it[VacacionesTable.fechaFin],
                    estado = it[VacacionesTable.estado]
                )
            }
    }

    private fun calcularRestantesVacaciones(
        userId: Int,
        anio: Int
    ): Triple<Int, Int, Int> {

        val resumen =
            obtenerResumen(userId, anio)

        val usadosNavidad =
            resumen[VacacionesResumenTable.diasNavidadUsados]

        val usadosLibres =
            resumen[VacacionesResumenTable.diasLibresUsados]

        val restantesNavidad =
            15 - usadosNavidad -
                    diasPendientesNavidad(userId)

        val restantesLibres =
            15 - usadosLibres -
                    diasPendientesLibres(userId)

        return Triple(
            restantesNavidad,
            restantesLibres,
            restantesNavidad + restantesLibres
        )
    }


    fun obtenerResumen(
        userId: Int,
        anio: Int
    ): ResultRow {

        val existente =
            VacacionesResumenTable
                .selectAll()
                .where {
                    (VacacionesResumenTable.userId eq userId) and
                            (VacacionesResumenTable.anio eq anio)
                }
                .singleOrNull()

        if (existente != null)
            return existente


        VacacionesResumenTable.insert {

            it[VacacionesResumenTable.userId] = userId
            it[VacacionesResumenTable.anio] = anio
        }

        return VacacionesResumenTable
            .selectAll()
            .where {
                (VacacionesResumenTable.userId eq userId) and
                        (VacacionesResumenTable.anio eq anio)
            }
            .single()
    }

    fun obtenerResumenUsuario(
        userId: Int
    ): VacacionesResumenResponse = transaction {

        val anio =
            LocalDate.now().year

        val resumen =
            obtenerResumen(userId, anio)

        val usadosNavidad =
            resumen[VacacionesResumenTable.diasNavidadUsados]

        val usadosLibres =
            resumen[VacacionesResumenTable.diasLibresUsados]

        val (
            restantesNavidad,
            restantesLibres,
            restantesTotales
        ) = calcularRestantesVacaciones(userId, anio)

        VacacionesResumenResponse(

            anio = anio,

            diasNavidadUsados = usadosNavidad,
            diasNavidadRestantes = restantesNavidad,

            diasLibresUsados = usadosLibres,
            diasLibresRestantes = restantesLibres,

            diasTotalesRestantes = restantesTotales
        )
    }

    fun obtenerResumenTodosUsuarios():
            List<VacacionesResumenAdminResponse> = transaction {

        val anio =
            LocalDate.now().year

        UsuariosTable
            .selectAll()
            .map { usuario ->

                val userId =
                    usuario[UsuariosTable.id]

                val username =
                    usuario[UsuariosTable.username]

                val (
                    restantesNavidad,
                    restantesLibres,
                    restantesTotales
                ) = calcularRestantesVacaciones(userId, anio)

                VacacionesResumenAdminResponse(

                    userId = userId,
                    username = username,

                    diasNavidadRestantes = restantesNavidad,
                    diasLibresRestantes = restantesLibres,

                    diasTotalesRestantes = restantesTotales
                )
            }
    }
    fun obtenerAlertasNavidad():
            List<VacacionesAlertaResponse> = transaction {

        val hoy =
            LocalDate.now()

        if (hoy.monthValue < 11)
            return@transaction emptyList()


        val anio =
            hoy.year

        val urgente =
            hoy.monthValue == 12


        UsuariosTable
            .selectAll()
            .mapNotNull { usuario ->

                val userId =
                    usuario[UsuariosTable.id]

                val username =
                    usuario[UsuariosTable.username]

                val resumen =
                    obtenerResumen(userId, anio)

                val usadosNavidad =
                    resumen[VacacionesResumenTable.diasNavidadUsados]

                val pendientesNavidad =
                    diasPendientesNavidad(userId)

                val restantes =
                    15 - usadosNavidad - pendientesNavidad


                if (restantes > 0) {

                    VacacionesAlertaResponse(

                        userId = userId,
                        username = username,

                        diasNavidadUsados = usadosNavidad,
                        diasNavidadPendientes = pendientesNavidad,

                        diasNavidadRestantes = restantes,

                        urgente = urgente
                    )

                } else null
            }
    }

    fun actualizarEstado(
        id: Int,
        nuevoEstado: String
    ) = transaction {

        val estadoEnum =
            nuevoEstado.toEstadoVacacionesOrNull()
                ?: error("Estado de vacaciones inválido")

        val vacaciones =
            VacacionesTable
                .selectAll()
                .where { VacacionesTable.id eq id }
                .single()

        VacacionesTable.update(
            { VacacionesTable.id eq id }
        ) {
            it[estado] =
                estadoEnum.name.lowercase()
        }

        if (estadoEnum != EstadoVacaciones.APROBADO)
            return@transaction

        val userId =
            vacaciones[VacacionesTable.userId]

        val inicio =
            LocalDate.parse(
                vacaciones[VacacionesTable.fechaInicio]
            )

        val fin =
            LocalDate.parse(
                vacaciones[VacacionesTable.fechaFin]
            )

        val dias =
            ChronoUnit.DAYS.between(inicio, fin).toInt() + 1

        val resumen =
            obtenerResumen(userId, inicio.year)

        val resumenId =
            resumen[VacacionesResumenTable.id]

        if (vacaciones[VacacionesTable.tipo] == "navidad") {

            VacacionesResumenTable.update({
                VacacionesResumenTable.id eq resumenId
            }) {

                it[diasNavidadUsados] =
                    resumen[VacacionesResumenTable.diasNavidadUsados] + dias
            }

        } else {

            VacacionesResumenTable.update({
                VacacionesResumenTable.id eq resumenId
            }) {

                it[diasLibresUsados] =
                    resumen[VacacionesResumenTable.diasLibresUsados] + dias
            }
        }
    }


    fun esPeriodoNavidad(
        inicio: LocalDate,
        fin: LocalDate
    ): Boolean {

        val inicioNavidad =
            LocalDate.of(inicio.year, 12, 23)

        val finNavidad =
            LocalDate.of(inicio.year + 1, 1, 7)

        return !inicio.isBefore(inicioNavidad)
                && !fin.isAfter(finNavidad)
    }


    fun diasPendientesNavidad(
        userId: Int
    ): Int {

        return VacacionesTable
            .selectAll()
            .where {
                (VacacionesTable.userId eq userId) and
                        (VacacionesTable.estado eq "pendiente") and
                        (VacacionesTable.tipo eq "navidad")
            }
            .sumOf {

                val inicio =
                    LocalDate.parse(it[VacacionesTable.fechaInicio])

                val fin =
                    LocalDate.parse(it[VacacionesTable.fechaFin])

                ChronoUnit.DAYS.between(inicio, fin).toInt() + 1
            }
    }


    fun diasPendientesLibres(
        userId: Int
    ): Int {

        return VacacionesTable
            .selectAll()
            .where {
                (VacacionesTable.userId eq userId) and
                        (VacacionesTable.estado eq "pendiente") and
                        (VacacionesTable.tipo eq "libre")
            }
            .sumOf {

                val inicio =
                    LocalDate.parse(it[VacacionesTable.fechaInicio])

                val fin =
                    LocalDate.parse(it[VacacionesTable.fechaFin])

                ChronoUnit.DAYS.between(inicio, fin).toInt() + 1
            }
    }
}