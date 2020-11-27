package com.kreait.bots.agile.core.auth

import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
class WebConfiguration(@Value("\${authentication.key}") private val authKey: String) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(AuthenticationRequestInterceptor(authKey))
    }
}

class AuthenticationRequestInterceptor(private val authKey: String) : HandlerInterceptorAdapter() {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (handler is HandlerMethod && handler.hasMethodAnnotation(Authorize::class.java)) {

            if (String(Base64.decodeBase64(request.getHeader("Authorization")
                            ?: throw UnauthorizedException()), Charset.forName("UTF-8")).trim() != this.authKey) throw UnauthorizedException()
        }

        return true
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException : RuntimeException()


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authorize
