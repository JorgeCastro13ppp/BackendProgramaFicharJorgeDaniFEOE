package com.empresa.fichaje.services

import com.empresa.fichaje.database.FichajesTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.FichajeResponse
import com.empresa.fichaje.models.HorasResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.innerJoin

class FichajeService {

    fun registrarFichaje(
        userId: Int,
        token: String,
        tipo: String
    ) {

        // Aquí podrías validar token QR si quieres reforzarlo más

        transaction {

            FichajesTable.insert {

                it[FichajesTable.userId] = userId
                it[FichajesTable.fechaHora] = System.currentTimeMillis()
                it[FichajesTable.tipo] = tipo
            }
        }
    }

    fun eliminarFichaje(id: Int) {

        transaction {

            FichajesTable.deleteWhere {
                FichajesTable.id eq id
            }
        }
    }


    fun obtenerFichajes(userId: Int): List<FichajeResponse> {

        return transaction {

            (FichajesTable innerJoin UsuariosTable)
                .selectAll()
                .filter {
                    it[FichajesTable.userId] == userId
                }
                .map {

                    FichajeResponse(
                        id = it[FichajesTable.id],
                        userId = it[FichajesTable.userId],
                        username = it[UsuariosTable.username],
                        fechaHora = it[FichajesTable.fechaHora],
                        tipo = it[FichajesTable.tipo]
                    )
                }
        }
    }
    fun horasMensuales(userId: Int): Double {

        return transaction {

            val now = java.time.LocalDate.now()

            val inicioMes = now.withDayOfMonth(1)
                .atStartOfDay()
                .toEpochSecond(java.time.ZoneOffset.UTC) * 1000

            val fichajes = FichajesTable
                .selectAll()
                .filter {
                    it[FichajesTable.userId] == userId &&
                            it[FichajesTable.fechaHora] >= inicioMes
                }
                .sortedBy { it[FichajesTable.fechaHora] }

            var entrada: Long? = null
            var totalHoras = 0.0

            for (f in fichajes) {

                if (f[FichajesTable.tipo] == "entrada") {
                    entrada = f[FichajesTable.fechaHora]
                }

                if (f[FichajesTable.tipo] == "salida" && entrada != null) {

                    val salida = f[FichajesTable.fechaHora]

                    totalHoras +=
                        (salida - entrada) / 1000.0 / 60.0 / 60.0

                    entrada = null
                }
            }

            totalHoras
        }
    }


    fun calcularHoras(userId: Int): List<HorasResponse> {

        return transaction {

            val fichajes = FichajesTable
                .selectAll()
                .filter { it[FichajesTable.userId] == userId }
                .sortedBy { it[FichajesTable.fechaHora] }

            val resultado = mutableListOf<HorasResponse>()

            var entrada: Long? = null

            for (f in fichajes) {

                if (f[FichajesTable.tipo] == "entrada") {
                    entrada = f[FichajesTable.fechaHora]
                }

                if (f[FichajesTable.tipo] == "salida" && entrada != null) {

                    val salida = f[FichajesTable.fechaHora]

                    val horas =
                        (salida - entrada) / 1000.0 / 60.0 / 60.0

                    resultado.add(
                        HorasResponse(
                            fecha = entrada.toString(),
                            horasTrabajadas = horas
                        )
                    )

                    entrada = null
                }
            }

            resultado
        }
    }

    fun obtenerTodos(): List<FichajeResponse> {

        return transaction {

            (FichajesTable innerJoin UsuariosTable)
                .selectAll()
                .map {

                    FichajeResponse(
                        id = it[FichajesTable.id],
                        userId = it[FichajesTable.userId],
                        username = it[UsuariosTable.username],
                        fechaHora = it[FichajesTable.fechaHora],
                        tipo = it[FichajesTable.tipo]
                    )
                }
        }
    }

    fun actualizarFichaje(
        id: Int,
        nuevaFecha: Long,
        nuevoTipo: String
    ) {

        transaction {

            FichajesTable.update({
                FichajesTable.id eq id
            }) {

                it[fechaHora] = nuevaFecha
                it[tipo] = nuevoTipo
            }
        }
    }

    fun crearFichajeManual(
        userId: Int,
        fechaHora: Long,
        tipo: String
    ) {

        transaction {

            FichajesTable.insert {

                it[FichajesTable.userId] = userId
                it[FichajesTable.fechaHora] = fechaHora
                it[FichajesTable.tipo] = tipo
            }
        }
    }
}