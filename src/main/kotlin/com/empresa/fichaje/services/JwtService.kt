package com.empresa.fichaje.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtService {

    private const val secret = "super-secret-key"
    private const val issuer = "fichaje-app"
    private const val audience = "fichaje-users"

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: Int, role: String): String {

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
            .sign(algorithm)
    }

    fun verifier() =
        JWT.require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
}