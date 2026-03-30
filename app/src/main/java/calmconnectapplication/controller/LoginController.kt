package com.example.calmconnect.controller

import com.example.calmconnect.util.Result

interface LoginController {
    fun validateLogin(username: String, password: String): Result<Unit>
}
