package com.example.domain.auth

import com.example.domain.httpUnauthenticated
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

data class AuthAccount(
    val id: Long,
)

class AuthAccountArgumentResolver(
    private val jwtService: JwtService,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getParameterType() == AuthAccount::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val token = webRequest.getHeader("Authorization")?.takeIf { it.isNotBlank() }
        if (token == null) {
            if (parameter.isOptional) return null
            httpUnauthenticated()
        }
        val jwt = jwtService.decodeAccess(token.removePrefix("Bearer "))
        return AuthAccount(jwt.subject.toLong())
    }
}

class DevAuthAccountArgumentResolver(
    private val resolver: AuthAccountArgumentResolver,
) : HandlerMethodArgumentResolver {
    private val logger = KotlinLogging.logger { }

    init {
        logger.warn { "Authorization 헤더에 숫자가 있는 경우, 검증하지 않고 accountId로 처리합니다. 반드시 개발용도로만 사용해주세요." }
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean = resolver.supportsParameter(parameter)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val token = webRequest.getHeader("Authorization")?.takeIf { it.isNotBlank() }?.removePrefix("Bearer ")
        val accountId = token?.toLongOrNull()
            ?: return resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        return AuthAccount(id = accountId)
    }
}
