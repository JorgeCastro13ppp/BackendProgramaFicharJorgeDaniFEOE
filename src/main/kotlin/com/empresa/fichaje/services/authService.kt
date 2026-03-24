package com.empresa.fichaje.services


import com.empresa.fichaje.models.LoginRequest
import com.empresa.fichaje.models.LoginResponse
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.User
import com.empresa.fichaje.models.UsuarioResponse
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

    fun obtenerUsuarios(): List<UsuarioResponse> {

        return transaction {

            UsuariosTable.selectAll().map {

                UsuarioResponse(
                    id = it[UsuariosTable.id],
                    username = it[UsuariosTable.username],
                    role = it[UsuariosTable.role]
                )
            }
        }
    }
}