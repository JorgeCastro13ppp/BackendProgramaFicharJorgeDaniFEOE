package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.DeviceTokensTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DeviceTokenService {

    fun save(
        userId: Int,
        token: String,
        platform: String
    ) {

        transaction {

            DeviceTokensTable.deleteWhere {

                DeviceTokensTable.token eq token
            }

            DeviceTokensTable.insert {

                it[DeviceTokensTable.userId] = userId
                it[DeviceTokensTable.token] = token
                it[DeviceTokensTable.platform] = platform
                it[DeviceTokensTable.updatedAt] =
                    System.currentTimeMillis()
            }

            println("TOKEN GUARDADO: $token")
        }
    }

    fun getTokens(userId: Int): List<String> =
        transaction {

            DeviceTokensTable
                .selectAll()
                .where { DeviceTokensTable.userId eq userId }
                .map { it[DeviceTokensTable.token] }
        }
}