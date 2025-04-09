package com.example

interface AuthProvider {
    val accessToken: String
    val refreshToken: String
    val idToken: String
}

data class DefaultAuthProvider(
    override val accessToken: String,
    override val refreshToken: String,
    override val idToken: String,
) : AuthProvider
