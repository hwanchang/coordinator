package com.coordinator.common.api

sealed class ApiResponse<T> {
    data class Success<T>(val isSucceeded: Boolean = true, val result: T) : ApiResponse<T>()

    data class Failure<T>(val isSucceeded: Boolean = false, val errorMessage: String) : ApiResponse<T>()
}
