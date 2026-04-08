package com.empresa.fichaje.database

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
            SchemaUtils.create( UsuariosTable, DocumentosTable, VacacionesTable, FaltasTable,
                FichajesEventosTable)
        }
    }
}