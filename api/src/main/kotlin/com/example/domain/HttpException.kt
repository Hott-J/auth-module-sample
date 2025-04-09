package com.example.domain

import org.springframework.http.HttpStatus

class HttpException(
    val status: HttpStatus,
    val responseMessage: String,
    val errorCode: String? = null,
    message: String? = null,
) : RuntimeException(message?.let { "$status $it" } ?: status.toString())

/**
 * 사용자가 잘못된 요청을 했을때 권장하는 에러 응답 입니다.
 *
 * [request] 는 요청 전체를 logging 하고 싶을때 설정하시면 됩니다.
 */
fun httpBadRequest(
    key: String,
    value: Any?,
    request: Any? = null,
    errorCode: String? = null,
): Nothing = throw HttpException(HttpStatus.BAD_REQUEST, "$key value $value invalid", errorCode, request?.toString())

fun httpBadRequest(
    responseMessage: String,
    message: String? = null,
    errorCode: String? = null,
): Nothing = throw HttpException(HttpStatus.BAD_REQUEST, responseMessage, errorCode, message)

fun httpUnauthenticated(
    responseMessage: String = "invalid token",
    message: String? = null,
    errorCode: String? = null,
): Nothing = throw HttpException(HttpStatus.UNAUTHORIZED, responseMessage, errorCode, message)

fun httpUnauthorized(
    responseMessage: String = "permission denied",
    message: String? = null,
    errorCode: String? = null,
): Nothing = throw HttpException(HttpStatus.FORBIDDEN, responseMessage, errorCode, message)

/**
 * 요청한 경로에 데이터 없음을 응답합니다.
 * url 의 path variable 를 key 로 사용하는 main entity 를 찾을 수 없을때 사용해야 하며 그 외에는 다른 에러를 응답하는걸 권장합니다.
 * - 유저가 복구 가능한 에러 : [httpBadRequest]
 * - 회사만 복구 가능한 에러 : [httpInternalServerError]
 *
 * [responseMessage] 를 지정하지 않으면 기본 메시지로 응답합니다.
 */
fun httpNotFound(
    responseMessage: String? = null,
    errorCode: String? = null,
): Nothing = throw HttpException(HttpStatus.NOT_FOUND, responseMessage ?: "resource does not exist", errorCode)

/**
 * 유저가 복구 불가능한 에러가 발생한 경우 사용합니다.
 *
 * 응답 메시지는 고정이며 [message] 파라메터로 server 측 log 를 기록할 수 있습니다.
 */
fun httpInternalServerError(
    message: String,
    errorCode: String? = null,
): Nothing = throw HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error", errorCode, message)

