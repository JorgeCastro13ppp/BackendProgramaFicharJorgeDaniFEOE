package com.empresa.fichaje.services


import com.empresa.fichaje.models.LoginRequest
import com.empresa.fichaje.models.LoginResponse
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.User
import com.empresa.fichaje.models.UsuarioResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AuthService {

    fun login(request: LoginRequest): User? {

        return transaction {

            UsuariosTable.selectAll()
                .find { row ->

                    row[UsuariosTable.username] == request.username &&
                            SecurityService.verifyPassword(
                                request.password,
                                row[UsuariosTable.password]
                            )
                }
                ?.let {

                    User(
                        id = it[UsuariosTable.id],
                        username = it[UsuariosTable.username],
                        role = it[UsuariosTable.role]
                    )
                }
        }
    }

    fun register(username: String, password: String): Boolean {

        val hashedPassword = SecurityService.hashPassword(password)

        return transaction {
            UsuariosTable.insert {
                it[UsuariosTable.username] = username
                it[UsuariosTable.password] = hashedPassword
                it[UsuariosTable.role] = "worker" // por defecto
            }
            true
        }
    }

    fun obtenerUsuarios(
        role: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<UsuarioResponse> {

        return transaction {

            var query =
                UsuariosTable.selectAll()


            /*
            ========================
            FILTRO POR ROL
            ========================
            */

            if (role != null && role != "todos") {

                query =
                    query.andWhere {
                        UsuariosTable.role eq role
                    }
            }


            /*
            ========================
            ORDENACIÓN
            ========================
            */

            val sortColumn = when (sortBy) {

                "username" -> UsuariosTable.username
                "role" -> UsuariosTable.role
                "id" -> UsuariosTable.id

                else -> UsuariosTable.username
            }


            val sortOrder =
                if (order == "desc")
                    org.jetbrains.exposed.sql.SortOrder.DESC
                else
                    org.jetbrains.exposed.sql.SortOrder.ASC


            query =
                query.orderBy(sortColumn to sortOrder)


            query.map {

                UsuarioResponse(
                    id = it[UsuariosTable.id],
                    username = it[UsuariosTable.username],
                    role = it[UsuariosTable.role]
                )
            }
        }
    }

    fun eliminarUsuario(id: Int): Boolean {

        return transaction {

            UsuariosTable.deleteWhere {
                UsuariosTable.id eq id
            } > 0
        }
    }
}