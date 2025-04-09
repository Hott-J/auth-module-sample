package com.example.domain.auth

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class AuthServiceTest {
    private val jwtService: JwtService = mockk()
    private val authService = AuthService(jwtService)

    @Test
    fun `Account ID로 액세스 토큰과 리프레시 토큰을 생성한다`() {
        val accountId = 123L
        val email = "test@test.com"
        val oauthProvider = "google"
        val idToken = "id-token-123"
        val accessToken = "access-token-123"
        val refreshToken = "refresh-token-123"

        every { jwtService.createId(accountId, email, oauthProvider) } returns idToken
        every { jwtService.createAccess(accountId, email, oauthProvider) } returns accessToken
        every { jwtService.createRefresh(accountId, email, oauthProvider) } returns refreshToken

        val result = authService.createTokens(accountId, email, oauthProvider)

        result.idToken shouldBe idToken
        result.accessToken shouldBe accessToken
        result.refreshToken shouldBe refreshToken
    }

    @Test
    fun `Account ID로 새로운 액세스 토큰과 리프레시 토큰을 갱신한다`() {
        val accountId = 456L
        val email = "test@test.com"
        val oauthProvider = "google"
        val newAccessToken = "new-access-token-456"
        val newRefreshToken = "new-refresh-token-456"

        every { jwtService.createAccess(accountId, email, oauthProvider) } returns newAccessToken
        every { jwtService.createRefresh(accountId, email, oauthProvider) } returns newRefreshToken

        val result = authService.refreshTokens(accountId, email, oauthProvider, false)

        result.idToken shouldBe null
        result.accessToken shouldBe newAccessToken
        result.refreshToken shouldBe newRefreshToken
    }

    @Test
    fun `Account ID, refreshTokenID param 으로 새로운 ID토큰과 리프레시 토큰을 갱신한다`() {
        val accountId = 456L
        val email = "test@test.com"
        val oauthProvider = "google"
        val newIdToken = "new-id-token-456"
        val newAccessToken = "new-access-token-456"
        val newRefreshToken = "new-refresh-token-456"

        every { jwtService.createId(accountId, email, oauthProvider) } returns newIdToken
        every { jwtService.createAccess(accountId, email, oauthProvider) } returns newAccessToken
        every { jwtService.createRefresh(accountId, email, oauthProvider) } returns newRefreshToken

        val result = authService.refreshTokens(accountId, email, oauthProvider, true)

        result.idToken shouldBe newIdToken
        result.accessToken shouldBe newAccessToken
        result.refreshToken shouldBe newRefreshToken
    }

    @Test
    fun `주어진 문자열을 SHA-256 알고리즘으로 해시 처리한다`() {
        val token = "example-token"
        val expectedHash = "4d1566a1d7df42a8517456d60ea06ed284e535cfe4c956aa6ee172dbcdf945f7"

        val result = authService.hashToken(token)

        result shouldBe expectedHash
    }
}
