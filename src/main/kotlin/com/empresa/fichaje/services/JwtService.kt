package com.empresa.fichaje.services

// Librería oficial para trabajar con JWT en Java/Kotlin
import com.auth0.jwt.JWT

// Permite verificar tokens entrantes (login ya realizado)
import com.auth0.jwt.JWTVerifier

// Define el algoritmo de firma del token
import com.auth0.jwt.algorithms.Algorithm

// Enum interno del dominio con roles del sistema
import com.empresa.fichaje.domain.enums.Role

// Necesario para manejar fechas de expiración
import java.util.*

object JwtService {

    /*
    ========================
    CLAVE SECRETA JWT
    ========================
    */

    // Se obtiene desde variable de entorno por seguridad
    // Si no existe (modo desarrollo), usa una clave fallback
    private val secret =
        System.getenv("JWT_SECRET")
            ?: "dev-secret-key"

    /*
    IMPORTANTE:
    En producción SIEMPRE debe existir JWT_SECRET
    para evitar ataques de falsificación de tokens.
    */


    /*
    ========================
    METADATA DEL TOKEN
    ========================
    */

    // Identifica quién generó el token
    private const val ISSUER =
        "fichaje-app"

    // Identifica para quién está destinado el token
    private const val AUDIENCE =
        "fichaje-users"

    /*
    ISSUER y AUDIENCE permiten:

    ✔ evitar reutilización del token en otro sistema
    ✔ validar origen del token
    ✔ mejorar seguridad del backend
    */


    /*
    ========================
    TIEMPOS DE EXPIRACIÓN
    ========================
    */

    // Tiempo máximo sesión trabajador
    private const val WORKER_EXPIRATION_MILLIS =
        24 * 60 * 60 * 1000L // 24 horas

    // Tiempo máximo sesión administrador
    private const val ADMIN_EXPIRATION_MILLIS =
        8 * 60 * 60 * 1000L // 8 horas

    /*
    Buena práctica de seguridad:

    ADMIN → sesiones más cortas
    WORKER → sesiones más largas

    Reduce impacto si roban token admin.
    */


    /*
    ========================
    ALGORITMO DE FIRMA
    ========================
    */

    // Se usa HMAC SHA256 para firmar tokens
    private val algorithm =
        Algorithm.HMAC256(secret)

    /*
    HMAC256 = estándar industria

    Garantiza:

    ✔ integridad del token
    ✔ autenticidad del emisor
    ✔ protección contra manipulación
    */


    /*
    ========================
    EXPIRACIÓN SEGÚN ROL
    ========================
    */

    // Decide automáticamente cuánto dura el token
    private fun expirationForRole(
        role: Role
    ): Long =
        if (role == Role.ADMIN)
            ADMIN_EXPIRATION_MILLIS
        else
            WORKER_EXPIRATION_MILLIS

    /*
    Permite política de seguridad dinámica:

    ADMIN → sesión corta
    USER → sesión larga

    Diseño elegante y escalable.
    */


    /*
    ========================
    GENERACIÓN DE TOKEN JWT
    ========================
    */

    fun generateToken(
        userId: Int,
        role: Role
    ): String =

        JWT.create()

            // Define destinatario del token
            .withAudience(AUDIENCE)

            // Define quién lo emitió
            .withIssuer(ISSUER)

            // Guarda ID usuario dentro del token
            .withClaim("userId", userId)

            // Guarda rol dentro del token
            .withClaim("role", role.name)

            // Define expiración automática del token
            .withExpiresAt(
                Date(
                    System.currentTimeMillis() +
                            expirationForRole(role)
                )
            )

            // Firma el token con clave secreta
            .sign(algorithm)

    /*
    Resultado:

    Token firmado que contiene:

    {
        userId: X,
        role: ADMIN|WORKER,
        iss: fichaje-app,
        aud: fichaje-users,
        exp: timestamp
    }

    No requiere consultar BD en cada request.
    */


    /*
    ========================
    VERIFICADOR DE TOKENS
    ========================
    */

    val verifier: JWTVerifier =
        JWT.require(algorithm)

            // Verifica audiencia correcta
            .withAudience(AUDIENCE)

            // Verifica emisor correcto
            .withIssuer(ISSUER)

            // Construye verificador final
            .build()

    /*
    Este verifier se usa en:

    Ktor Authentication config

    para validar:

    ✔ firma correcta
    ✔ issuer válido
    ✔ audience válida
    ✔ expiración válida

    antes de aceptar cualquier request protegida.
    */
}