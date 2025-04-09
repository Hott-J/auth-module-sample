package com.example.domain.auth

import com.example.DefaultAuthProvider
import com.example.IntegrationTest
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class AuthControllerTest : IntegrationTest() {

    /** [AuthController.signIn] */
    @Test
    fun `회원가입 할 수 있다`() {
        auth.signIn("", "", "11", "1234").should {
            it.idToken shouldNotBe null
            it.accessToken shouldNotBe null
            it.refreshToken shouldNotBe null
        }
    }

    /** [AuthController.refresh] */
    @Test
    fun `access, refresh, id 토큰을 갱신 할 수 있다`() {
        val user = auth.signInRandom()

        Thread.sleep(1000)
        val token = auth.refresh(auth = user)
        token.should {
            it.idToken shouldBe null
            it.accessToken shouldNotBe user.accessToken
            it.refreshToken shouldNotBe user.refreshToken
        }

        Thread.sleep(1000)

        auth.refresh(refreshIdToken = true, auth = DefaultAuthProvider("", token.refreshToken, "")).should {
            it.idToken shouldNotBe null
            it.accessToken shouldNotBe user.accessToken
            it.refreshToken shouldNotBe user.refreshToken
        }
    }
}
