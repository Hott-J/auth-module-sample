package com.example

interface ApiClient {
    val auth: AuthApiClient

    fun withAuth(auth: AuthProvider): ApiClient
}

class ApiException(
    val httpStatus: Int,
    val errorCode: String?,
    message: String
) : RuntimeException(message)
