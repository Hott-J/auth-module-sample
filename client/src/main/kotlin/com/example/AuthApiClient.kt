package com.example

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApiClient(http: io.ktor.client.HttpClient) : AbstractApiClient(http, null) {
    fun signIn(clientId: String, clientInfo: String, email: String, auth: String, oauthProvider: String = "google") = call<SignInResponse> {
        http.post("/api/auth/signin") {
            bearerAuth(auth)
            setBody(mapOf(
                "clientId" to clientId,
                "clientInfo" to clientInfo,
                "email" to email,
                "oauthProvider" to oauthProvider
            ))
        }
    }

    fun refresh(refreshIdToken: Boolean? = null, auth: AuthProvider? = null) = call<RefreshTokensResponse> {
        http.post("/api/auth/refresh") {
            withRefreshToken(auth)
            parameter("refreshIdToken", refreshIdToken)
        }
    }
}

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
