package ecommerce.config.interceptor

import ecommerce.exception.AuthorizationException
import ecommerce.infrastructure.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class CheckLoginInterceptor(private val jwtTokenProvider: JwtTokenProvider) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val header = request.getHeader("Authorization") ?: throw AuthorizationException("Unauthorized")
        val token = header.removePrefix("Bearer ").trim()

        return if (jwtTokenProvider.validateToken(token)){
            true
        } else throw AuthorizationException("Unauthorized")
    }
}