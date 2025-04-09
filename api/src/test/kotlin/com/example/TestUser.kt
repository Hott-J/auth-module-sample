package com.example

data class TestUser(
    val id: Long,
    override val accessToken: String,
    override val refreshToken: String,
    override val idToken: String,
) : AuthProvider
