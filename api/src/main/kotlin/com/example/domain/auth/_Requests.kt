package com.example.domain.auth

data class AuthRequest(
    val clientId: String,
    val clientInfo: String,
    val email: String,
    val oauthProvider: String
)

