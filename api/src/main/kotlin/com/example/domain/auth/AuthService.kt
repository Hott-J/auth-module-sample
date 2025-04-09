package com.example.domain.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtService: JwtService,
) {

    fun createTokens(accountId: Long, email: String, oauthProvider: String): CreateTokensResponse {
        val idToken = jwtService.createId(accountId, email, oauthProvider)
        val accessToken = jwtService.createAccess(accountId, email, oauthProvider)
        val refreshToken = jwtService.createRefresh(accountId, email, oauthProvider)
        return CreateTokensResponse(
            idToken = idToken,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun refreshTokens(
        accountId: Long,
        email: String,
        oauthProvider: String,
        refreshIdToken: Boolean
    ): RefreshTokensResponse {
        val idToken = if (refreshIdToken) {
            jwtService.createId(accountId, email, oauthProvider)
        } else {
            null
        }
        val accessToken = jwtService.createAccess(accountId, email, oauthProvider)
        val refreshToken = jwtService.createRefresh(accountId, email, oauthProvider)
        return RefreshTokensResponse(
            idToken = idToken,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}

data class CreateTokensResponse(
    val idToken: String,
    val accessToken: String,
    val refreshToken: String,
)
