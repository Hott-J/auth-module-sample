package com.example

import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.time.Duration.Companion.milliseconds
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.util.AttributeKey

class ExampleApiHttpClient(
    private val http: HttpClient,
    auth: AuthProvider? = null
) : ApiClient {
    constructor(defaultUrl: String, auth: AuthProvider? = null) : this(httpClient(defaultUrl), auth)

    override val auth: AuthApiClient = AuthApiClient(http)

    override fun withAuth(auth: AuthProvider): ApiClient = ExampleApiHttpClient(http, auth)

    companion object {
        fun httpClient(defaultUrl: String) = HttpClient {
            defaultRequest {
                url(defaultUrl)
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }
            install(CallLogger) { logger = KotlinLogging.logger {} }
        }
    }

    private class CallLogger(private val logger: KLogger) {

        class Config(var logger: KLogger? = null)

        companion object Plugin : HttpClientPlugin<Config, CallLogger> {
            override val key: AttributeKey<CallLogger> = AttributeKey("CallLogger")

            override fun prepare(block: Config.() -> Unit): CallLogger {
                return CallLogger(Config().apply(block).logger ?: KotlinLogging.logger {})
            }

            override fun install(plugin: CallLogger, scope: HttpClient) {
                scope.plugin(HttpSend).intercept { request ->
                    val id = Random.nextUInt()
                    plugin.logger.debug { "[$id] <<< request ${request.method.value} ${request.url.buildString()}" }
                    val call = execute(request)
                    plugin.logger.debug { "[$id] >>> response ${call.response.status.text} ${call.response.elapsed}" }
                    return@intercept call
                }
            }

            private val HttpStatusCode.text get() = "$value ${description.ifBlank { HttpStatusCode.fromValue(value).description }}"
            private val HttpResponse.elapsed get() = (call.response.responseTime.timestamp - call.response.requestTime.timestamp).milliseconds
        }
    }
}
