package com.example.configuration

import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

interface JWTAlgorithmFactory {
    fun createIdAlgorithm(): Algorithm
    fun createAccessAlgorithm(): Algorithm
    fun createRefreshAlgorithm(): Algorithm
}

@Configuration
data class JWTSignatureProperties(
    @Value("\${app.jwt.id.public-key}")
    val idPublicKey: String,

    @Value("\${app.jwt.id.private-key}")
    val idPrivateKey: String,

    @Value("\${app.jwt.access.private-key}")
    val accessPrivateKey: String,

    @Value("\${app.jwt.refresh.private-key}")
    val refreshPrivateKey: String
) : JWTAlgorithmFactory {
    override fun createIdAlgorithm(): Algorithm = Algorithm.RSA256(parseRSAPublicKey(), parseRSAPrivateKey())
    override fun createAccessAlgorithm(): Algorithm = Algorithm.HMAC256(accessPrivateKey)
    override fun createRefreshAlgorithm(): Algorithm = Algorithm.HMAC256(refreshPrivateKey)

    private fun parseRSAPublicKey(): RSAPublicKey {
        val publicKeyPEMFormatted = idPublicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")
        val decodedKey = Base64.getDecoder().decode(publicKeyPEMFormatted)
        val keySpec = X509EncodedKeySpec(decodedKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    private fun parseRSAPrivateKey(): RSAPrivateKey {
        val privateKeyPEMFormatted = idPrivateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")
        val decodedKey = Base64.getDecoder().decode(privateKeyPEMFormatted)
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }
}

@ConfigurationProperties(prefix = "app.jwt")
data class JWTProperties(
    val id: JWTKeyProperties,
    val access: JWTKeyProperties,
    val refresh: JWTKeyProperties,
)

data class JWTKeyProperties(
    val publicKey: String? = null,
    val privateKey: String,
    val issuer: String,
    val audience: String,
    /**
     * You can also use any of the supported units. These are:
     *
     * - ns for nanoseconds
     * - us for microseconds
     * - ms for milliseconds
     * - s for seconds
     * - m for minutes
     * - h for hours
     * - d for days
     *
     * ex
     * ```yaml
     * app.jwt.expiring-time: 100ms
     * ```
     */
    val expiringTime: Duration
)
