package ecommerce.interceptor

import ecommerce.handler.AuthorizationException
import ecommerce.infrastructure.JWTProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtAuthenticationInterceptor(
    val jwtProvider: JWTProvider,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val accessToken =
            request.getHeader("Authorization") ?: throw AuthorizationException("Authorization header missing")
        jwtProvider.validateToken(accessToken)
        return true
    }
}
