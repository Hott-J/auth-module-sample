package com.example

import kotlinx.coroutines.runBlocking
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.isSuccess
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

sealed class AbstractApiClient(
    protected val http: HttpClient,
    private val auth: AuthProvider?
) {
    protected inline fun <reified T> call(crossinline request: suspend () -> HttpResponse): T = runBlocking {
        val response = request()
        if (response.status.isSuccess()) {
            val result = response.body<ApiResult<T>>()
            result.data ?: throw ApiException(
                httpStatus = response.status.value,
                errorCode = null,
                message = "Response data is null"
            )
        } else {
            kidApiException(response)
        }
    }

    protected inline fun callEmptyBody(crossinline request: suspend () -> HttpResponse): Unit = runBlocking {
        val response = request()
        if (response.status.isSuccess()) Unit
        else kidApiException(response)
    }

    protected suspend fun kidApiException(response: HttpResponse): Nothing {
        val result = response.body<ApiResult<Unit>>()
        throw ApiException(
            httpStatus = response.status.value,
            errorCode = result.error?.code,
            message = "${response.request.method.value} ${response.request.url} : ${response.status.value} ${result.error?.message}"
        )
    }

    protected fun HttpMessageBuilder.withAccessToken(auth: AuthProvider? = null) {
        (auth ?: this@AbstractApiClient.auth)?.let { bearerAuth(it.accessToken) }
    }

    protected fun HttpMessageBuilder.withRefreshToken(auth: AuthProvider? = null) {
        (auth ?: this@AbstractApiClient.auth)?.let { bearerAuth(it.refreshToken) }
    }

    protected fun HttpMessageBuilder.withIdToken(auth: AuthProvider? = null) {
        (auth ?: this@AbstractApiClient.auth)?.let { bearerAuth(it.idToken) }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected data class ApiResult<R>(
        val data: R? = null,
        val error: Error? = null
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Error(
            val message: String,
            /** 에러 코드 */
            val code: String? = null,
        ) {
            companion object {
                @JvmStatic
                @JsonCreator
                fun fromString(value: String) = Error(message = value)
            }
        }
    }
}
