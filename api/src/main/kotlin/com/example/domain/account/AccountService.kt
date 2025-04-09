package com.example.domain.account

import com.example.domain.auth.AuthRequest
import org.springframework.stereotype.Service

@Service
class AccountService {
    fun getOrCreateAccount(request: AuthRequest): Account {
        // 임의의 계정 정보를 생성하여 반환
        return Account(
            id = 12345L,
            email = request.email,
            oauthProvider = request.oauthProvider
        )
    }

    fun registerDevice(accountId: Long, clientId: String, clientInfo: String, refreshTokenHash: String) {
        // 실제 디바이스 등록 로직은 생략
    }
}

data class Account(
    val id: Long,
    val email: String,
    val oauthProvider: String
)

data class AuthUser(
    val oauthProvider: String
) 