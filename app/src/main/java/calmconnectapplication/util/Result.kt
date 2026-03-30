package com.example.calmconnect.util

/**
 * A generic sealed class representing the outcome of an operation.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
