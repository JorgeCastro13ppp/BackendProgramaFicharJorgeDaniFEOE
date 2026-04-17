package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.domain.enums.Role
import com.empresa.fichaje.domain.models.User
import com.empresa.fichaje.dto.request.LoginRequest
import com.empresa.fichaje.dto.response.UsuarioResponse
import com.empresa.fichaje.utils.toRole
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class AuthService {

    fun login(
        request: LoginRequest
    ): User? = transaction {

        val userRow =
            UsuariosTable
                .selectAll()
                .where {
                    UsuariosTable.username eq request.username
                }
                .firstOrNull()

        userRow
            ?.takeIf {

                SecurityService.verifyPassword(
                    request.password,
                    it[UsuariosTable.password]
                )
            }
            ?.let {

                User(
                    id = it[UsuariosTable.id],
                    username = it[UsuariosTable.username],
                    role = it[UsuariosTable.role].toRole()
                )
            }
    }


    fun register(
        username: String,
        password: String
    ): Boolean = transaction {

        val exists =
            UsuariosTable
                .select(UsuariosTable.id)
                .where {
                    UsuariosTable.username eq username
                }
                .firstOrNull()

        if (exists != null)
            return@transaction false


        val hashedPassword =
            SecurityService.hashPassword(password)


        UsuariosTable.insert {

            it[UsuariosTable.username] = username
            it[UsuariosTable.password] = hashedPassword
            it[UsuariosTable.role] =
                Role.WORKER.name.lowercase()
        }

        true
    }


    fun obtenerUsuarios(
        role: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<UsuarioResponse> = transaction {

        val query =
            UsuariosTable
                .selectAll()
                .apply {

                    if (
                        role != null &&
                        role != "todos"
                    ) {

                        andWhere {
                            UsuariosTable.role eq role
                        }
                    }
                }


        val sortColumn =
            when (sortBy) {

                "username" -> UsuariosTable.username
                "role" -> UsuariosTable.role
                else -> UsuariosTable.id
            }


        val sortOrder =
            if (order == "desc")
                SortOrder.DESC
            else
                SortOrder.ASC


        query
            .orderBy(sortColumn to sortOrder)
            .map {

                UsuarioResponse(
                    id = it[UsuariosTable.id],
                    username = it[UsuariosTable.username],
                    role = it[UsuariosTable.role]
                )
            }
    }


    fun eliminarUsuario(
        id: Int
    ): Boolean = transaction {

        UsuariosTable.deleteWhere {
            UsuariosTable.id eq id
        } > 0
    }
}