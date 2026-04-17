package com.empresa.fichaje.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.empresa.fichaje.domain.enums.Role
import java.util.*

object JwtService {

    private val secret =
        System.getenv("JWT_SECRET")
            ?: "dev-secret-key"


    private const val ISSUER =
        "fichaje-app"

    private const val AUDIENCE =
        "fichaje-users"


    private const val WORKER_EXPIRATION_MILLIS =
        24 * 60 * 60 * 1000L // 24h

    private const val ADMIN_EXPIRATION_MILLIS =
        8 * 60 * 60 * 1000L // 8h


    private val algorithm =
        Algorithm.HMAC256(secret)


    private fun expirationForRole(
        role: Role
    ): Long =
        if (role == Role.ADMIN)
            ADMIN_EXPIRATION_MILLIS
        else
            WORKER_EXPIRATION_MILLIS


    fun generateToken(
        userId: Int,
        role: Role
    ): String =

        JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("role", role.name)
            .withExpiresAt(
                Date(
                    System.currentTimeMillis() +
                            expirationForRole(role)
                )
            )
            .sign(algorithm)


    val verifier: JWTVerifier =
        JWT.require(algorithm)
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .build()
}