package com.example.domain.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class JwkController(
    private val jwkService: JwkService
) {
    @GetMapping("/api/jwks-url")
    fun getJwksUrl(): Map<String, Any> {
        val publicKey = jwkService.convertPemToPublicKey()
        val jwk = jwkService.convertPublicKeyToJwk(publicKey)
        return mapOf("keys" to listOf(jwk))
    }

    @GetMapping("/api/jwks")
    fun getJwks(): Map<String, Any> {
        val publicKey = jwkService.convertPemToPublicKey()
        val jwk = jwkService.convertPublicKeyToJwk(publicKey)
        return mapOf("keys" to listOf(jwk))
    }
}
