package com.example

import kotlin.random.Random
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.support.TransactionTemplate
import com.auth0.jwt.JWT
import com.example.configuration.PostgreConfiguration

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = [PostgreConfiguration::class])
class IntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var template: TransactionTemplate

    private val api by lazy { ExampleApiHttpClient("http://localhost:${port}") }
    val auth get() = api.auth

    fun useApi(auth: AuthProvider, callback: ApiClient.() -> Unit) {
        callback(api.withAuth(auth))
    }

    fun AuthApiClient.signInRandom(
        clientId: String? = null,
        clientInfo: String? = null,
        email: String? = null,
        auth: String? = null,
    ): TestUser {
        val random = Random.nextInt(1, Int.MAX_VALUE)
        return signIn(
            clientId = clientId ?: "", // 프론트에서 빈 문자열로 보내고 있으므로 동일하게 테스트
            clientInfo = clientInfo ?: "", // 프론트에서 빈 문자열로 보내고 있으므로 동일하게 테스트
            email = email ?: "integration-test-${random}@lightscale.io",
            auth = auth ?: random.toString(),
        ).run {
            val jwt = JWT.decode(idToken)
            TestUser(jwt.subject.toLong(), accessToken, refreshToken, idToken)
        }
    }
}
