package com.example.configuration

import com.example.domain.auth.AuthAccountArgumentResolver
import com.example.domain.auth.DevAuthAccountArgumentResolver
import com.example.domain.auth.JwtService
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val env: Environment,
    private val jwtService: JwtService,
    ) : WebMvcConfigurer {
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        val accessResolver = AuthAccountArgumentResolver(jwtService)
        if (env.matchesProfiles("!prod")) {
            argumentResolvers.add(DevAuthAccountArgumentResolver(accessResolver))
        } else {
            argumentResolvers.add(accessResolver)
        }
    }
}
