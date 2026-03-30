package com.example.calmconnect.controller.impl

import com.example.calmconnect.controller.LoginController
import com.example.calmconnect.util.Result

class LoginControllerImpl : LoginController {

    override fun validateLogin(username: String, password: String): Result<Unit> {
        if (username.isBlank()) {
            return Result.Error("Username cannot be empty")
        }
        if (password.isBlank()) {
            return Result.Error("Password cannot be empty")
        }
        return Result.Success(Unit)
    }
}
