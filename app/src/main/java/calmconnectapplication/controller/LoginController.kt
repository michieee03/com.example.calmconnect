package calmconnectapplication.controller

import calmconnectapplication.util.Result

interface LoginController {
    fun validateLogin(username: String, password: String): Result<Unit>
}
