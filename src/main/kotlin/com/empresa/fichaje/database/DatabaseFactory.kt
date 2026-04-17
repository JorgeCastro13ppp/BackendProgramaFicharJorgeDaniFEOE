package com.empresa.fichaje.database

import com.empresa.fichaje.database.tables.DocumentosTable
import com.empresa.fichaje.database.tables.FaltasTable
import com.empresa.fichaje.database.tables.FichajesEventosTable
import com.empresa.fichaje.database.tables.HorasExtrasTable
import com.empresa.fichaje.database.tables.JornadasLaboralesTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.database.tables.VacacionesResumenTable
import com.empresa.fichaje.database.tables.VacacionesTable
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    private val dotenv = dotenv()

    fun init() {

        val dbUrl = dotenv["DB_URL"] ?: error("DB_URL no definida")
        val dbUser = dotenv["DB_USER"]
        val dbPassword = dotenv["DB_PASSWORD"]

        Database.connect(
            url = dbUrl,
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )

        transaction {
            SchemaUtils.create(
                // usuarios
                UsuariosTable,

                // fichajes
                FichajesEventosTable,
                JornadasLaboralesTable,
                HorasExtrasTable,

                // vacaciones
                VacacionesTable,
                VacacionesResumenTable,

                // documentos
                DocumentosTable,

                // incidencias
                FaltasTable
            )
        }
    }
}