package com.empresa.fichaje.services


import com.empresa.fichaje.models.LoginRequest
import com.empresa.fichaje.models.LoginResponse

class AuthService {

    fun login(request: LoginRequest): LoginResponse? {

        if (request.username == "admin" && request.password == "1234") {
            return LoginResponse("Login correcto", 1)
        }

        return null
    }
}