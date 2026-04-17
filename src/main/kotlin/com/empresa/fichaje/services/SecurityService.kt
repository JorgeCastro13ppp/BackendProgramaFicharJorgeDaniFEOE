package com.empresa.fichaje.services

import org.mindrot.jbcrypt.BCrypt

object SecurityService {

    private const val BCRYPT_COST = 12

    fun hashPassword(
        password: String
    ): String =
        BCrypt.hashpw(
            password,
            BCrypt.gensalt(BCRYPT_COST)
        )

    fun verifyPassword(
        password: String,
        hashedPassword: String
    ): Boolean =
        BCrypt.checkpw(
            password,
            hashedPassword
        )
}