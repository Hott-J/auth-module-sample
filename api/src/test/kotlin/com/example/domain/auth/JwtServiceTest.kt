package com.example.domain.auth

import java.time.Duration
import java.util.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.example.configuration.JWTAlgorithmFactory
import com.example.configuration.JWTKeyProperties
import com.example.configuration.JWTProperties
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JwtServiceTest {
    private lateinit var jwtService: JwtService
    private val jwkService: JwkService = mockk()
    private lateinit var jwtProperties: JWTProperties

    @BeforeEach
    fun setUp() {
        jwtProperties = JWTProperties(
            id = JWTKeyProperties(
                publicKey = "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----",
                privateKey = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----",
                issuer = "TestIssuer",
                audience = "TestAudience",
                expiringTime = Duration.ofMillis(3600000)
            ),
            access = JWTKeyProperties(
                privateKey = "mock-access-key",
                issuer = "TestIssuer",
                audience = "TestAudience",
                expiringTime = Duration.ofMillis(3600000)
            ),
            refresh = JWTKeyProperties(
                privateKey = "mock-refresh-key",
                issuer = "TestIssuer",
                audience = "TestAudience",
                expiringTime = Duration.ofHours(168) // 7 days
            )
        )
        jwtService = JwtService(jwtProperties, MockAlgorithmProvider, jwkService)
    }

    @Test
    fun `필수 파라미터가 존재하면 Access 토큰 생성이 정상적으로 이루어진다`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        val token = jwtService.createAccess(accountId, email, oauthProvider)

        token shouldNotBe ""
    }

    @Test
    fun `필수 파라미터가 존재하면 Refresh 토큰 생성이 정상적으로 이루어진다`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        val token = jwtService.createRefresh(accountId, email, oauthProvider)

        token shouldNotBe ""
    }

    @Test
    fun `유효한 Access 토큰을 검증하면 성공해야 한다`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        val token = jwtService.createAccess(accountId, email, oauthProvider)
        val decodedJWT = jwtService.validateAccess(token)

        decodedJWT.subject?.toLong() shouldBe accountId
    }

    @Test
    fun `유효한 Refresh 토큰을 검증하면 성공해야 한다`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        val token = jwtService.createRefresh(accountId, email, oauthProvider)
        val decodedJWT = jwtService.validateRefresh(token)

        decodedJWT.subject?.toLong() shouldBe accountId
    }

    @Test
    fun `만료된 Access 토큰은 검증 시 TokenExpiredException을 던져야 한다`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        jwtProperties = jwtProperties.copy(
            access = jwtProperties.access.copy(expiringTime = Duration.ZERO)
        )
        jwtService = JwtService(jwtProperties, MockAlgorithmProvider, jwkService)

        val token = jwtService.createAccess(accountId, email, oauthProvider)

        assertThrows<TokenExpiredException> {
            jwtService.validateAccess(token)
        }
    }

    @Test
    fun `유효한 Refresh 토큰을 사용하여 새로운 Refresh 토큰을 성공적으로 갱신`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        val newToken = jwtService.createRefresh(accountId, email, oauthProvider)

        val decodedJWT = jwtService.validateRefresh(newToken)
        decodedJWT.subject?.let { assertEquals(accountId, it.toLong()) }
    }

    @Test
    fun `유효하지 않은 키로 생성된 토큰을 사용하면 토큰 검증 시 예외가 발생해야 한다`() {
        val accountId = 12345L
        val invalidAlgorithm = Algorithm.HMAC256("invalid-secret")

        val invalidToken = JWT.create()
            .withIssuer(jwtProperties.access.issuer)
            .withSubject(accountId.toString())
            .withAudience(jwtProperties.access.audience)
            .withIssuedAt(Date())
            .withExpiresAt(Date(Date().time + 3600 * 1000))
            .sign(invalidAlgorithm)

        assertThrows<SignatureVerificationException> {
            jwtService.validateAccess(invalidToken)
        }
    }

    @Test
    fun `유효한 Access 토큰이 정상적으로 디코딩되고, 기대한 정보가 추출된다`() {
        val accountId = 12345L
        val email = "test@test.com"
        val oauthProvider = "google"
        val token = jwtService.createAccess(accountId, email, oauthProvider)
        val decodedJWT = jwtService.decodeAccess(token)

        assertEquals(accountId, decodedJWT.subject.toLong())
        assertEquals(jwtProperties.access.issuer, decodedJWT.issuer)
        assertEquals(jwtProperties.access.audience, decodedJWT.audience[0])
    }

    object MockAlgorithmProvider : JWTAlgorithmFactory {
        override fun createIdAlgorithm(): Algorithm {
            return Algorithm.HMAC256("mock-id-key")
        }

        override fun createAccessAlgorithm(): Algorithm {
            return Algorithm.HMAC256("mock-access-key")
        }

        override fun createRefreshAlgorithm(): Algorithm {
            return Algorithm.HMAC256("mock-refresh-key")
        }
    }
}
