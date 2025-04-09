package com.example.domain.auth

import com.example.domain.ApiResult
import com.example.domain.account.AccountService
import com.example.domain.httpUnauthorized
import com.example.domain.toResult
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(
    private val authService: AuthService,
    private val accountService: AccountService,
    private val jwtService: JwtService,
) {
    @PostMapping("/api/auth/signin")
    fun signIn(
        @RequestHeader("Authorization") authorization: String?,
        @RequestBody request: AuthRequest
    ): ApiResult<SignInResponse> {
        val account = accountService.getOrCreateAccount(request)
        val tokens = authService.createTokens(account.id, request.email, request.oauthProvider)
        accountService.registerDevice(
            accountId = account.id,
            clientId = request.clientId,
            clientInfo = request.clientInfo,
            refreshTokenHash = authService.hashToken(tokens.refreshToken)
        )
        return SignInResponse(
            idToken = tokens.idToken,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        ).toResult()
    }

    @PostMapping("/api/auth/refresh")
    fun refresh(
        @RequestHeader("Authorization") authorization: String?,
        @RequestParam refreshIdToken: Boolean = false
    ): ApiResult<RefreshTokensResponse> {
        val token = authorization?.removePrefix("Bearer ") ?: httpUnauthorized()
        val decodedJWT = jwtService.decodeRefresh(token)
        return authService.refreshTokens(
            decodedJWT.subject.toLong(),
            decodedJWT.getClaim("email").asString(),
            decodedJWT.getClaim("oauth_provider").asString(),
            refreshIdToken
        ).toResult()
    }
}
