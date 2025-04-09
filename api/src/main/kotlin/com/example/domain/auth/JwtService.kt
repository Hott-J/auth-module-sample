package com.example.domain.auth

import java.time.Instant
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.example.configuration.JWTAlgorithmFactory
import com.example.configuration.JWTProperties
import com.example.domain.httpUnauthenticated
import org.springframework.stereotype.Service

@Service
class JwtService(
    private val properties: JWTProperties,
    jwtAlgorithmFactory: JWTAlgorithmFactory,
    private val jwkService: JwkService,
) {
    private val idAlgorithm = jwtAlgorithmFactory.createIdAlgorithm()
    private val accessAlgorithm = jwtAlgorithmFactory.createAccessAlgorithm()
    private val refreshAlgorithm = jwtAlgorithmFactory.createRefreshAlgorithm()

    fun createId(accountId: Long, email: String, oauthProvider: String): String {
        val publicKey = jwkService.convertPemToPublicKey()
        val jwk = jwkService.convertPublicKeyToJwk(publicKey)
        val now = Instant.now()
        return JWT.create()
            .withHeader(mapOf("example" to jwk["example"] as String))
            .withIssuer(properties.id.issuer)
            .withSubject(accountId.toString())
            .withAudience(properties.id.audience)
            .withIssuedAt(now)
            .withExpiresAt(now.plus(properties.id.expiringTime))
            .withClaim("email", email)
            .withClaim("oauth_provider", oauthProvider)
            .sign(idAlgorithm)
    }

    fun createAccess(accountId: Long, email: String, oauthProvider: String): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(properties.access.issuer)
            .withSubject(accountId.toString())
            .withAudience(properties.access.audience)
            .withIssuedAt(now)
            .withExpiresAt(now.plus(properties.access.expiringTime))
            .withClaim("email", email)
            .withClaim("oauth_provider", oauthProvider)
            .sign(accessAlgorithm)
    }

    fun createRefresh(accountId: Long, email: String, oauthProvider: String): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(properties.refresh.issuer)
            .withSubject(accountId.toString())
            .withAudience(properties.refresh.audience)
            .withIssuedAt(now)
            .withExpiresAt(now.plus(properties.refresh.expiringTime))
            .withClaim("email", email)
            .withClaim("oauth_provider", oauthProvider)
            .sign(refreshAlgorithm)
    }


    fun validateId(token: String): DecodedJWT {
        val verifier: JWTVerifier = JWT.require(idAlgorithm)
            .withIssuer(properties.id.issuer)
            .build()
        return try {
            verifier.verify(token)
        } catch (e: JWTDecodeException) {
            httpUnauthenticated(message = "유효하지 않는 id token: $token")
        }
    }

    fun validateAccess(token: String): DecodedJWT {
        val verifier: JWTVerifier = JWT.require(accessAlgorithm)
            .withIssuer(properties.access.issuer)
            .build()
        return try {
            verifier.verify(token)
        } catch (e: JWTDecodeException) {
            httpUnauthenticated(message = "유효하지 않는 access token: $token")
        }
    }

    fun validateRefresh(token: String): DecodedJWT {
        val verifier: JWTVerifier = JWT.require(refreshAlgorithm)
            .withIssuer(properties.refresh.issuer)
            .build()
        return try {
            verifier.verify(token)
        } catch (e: JWTDecodeException) {
            httpUnauthenticated(message = "유효하지 않는 refresh token: $token")
        }
    }

    fun decodeId(token: String): DecodedJWT {
        validateId(token)
        return JWT.decode(token)
    }

    fun decodeAccess(token: String): DecodedJWT {
        validateAccess(token)
        return JWT.decode(token)
    }

    fun decodeRefresh(token: String): DecodedJWT {
        validateRefresh(token)
        return JWT.decode(token)
    }
}
