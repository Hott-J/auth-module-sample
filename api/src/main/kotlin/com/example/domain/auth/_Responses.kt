package com.example.domain.auth

data class SignInResponse(
    val idToken: String,
    val accessToken: String,
    val refreshToken: String,
)

data class RefreshTokensResponse(
    val idToken: String? = null,
    val accessToken: String,
    val refreshToken: String,
)
