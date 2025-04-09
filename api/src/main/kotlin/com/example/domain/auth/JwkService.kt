package com.example.domain.auth

import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwkService(
    @Value("\${app.jwt.id.public-key}") private val publicKey: String
) {
    fun convertPemToPublicKey(): RSAPublicKey {
        val sanitizedPem = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val encodedKey = Base64.getDecoder().decode(sanitizedPem)
        val keySpec = X509EncodedKeySpec(encodedKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    fun convertPublicKeyToJwk(publicKey: RSAPublicKey): Map<String, Any> {
        val modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.modulus.toByteArray())
        val exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.publicExponent.toByteArray())
        return mapOf(
            "kty" to "RSA",
            "alg" to "RS256",
            "use" to "sig",
            "example" to generateKeyId(publicKey),
            "n" to modulus,
            "e" to exponent
        )
    }

    private fun generateKeyId(publicKey: RSAPublicKey): String {
        val modulus = publicKey.modulus.toByteArray()
        val sha256 = java.security.MessageDigest.getInstance("SHA-256")
        val hash = sha256.digest(modulus)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}
