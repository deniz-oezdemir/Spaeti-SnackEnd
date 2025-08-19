package ecommerce.interceptor

import ecommerce.handler.AuthorizationException
import ecommerce.infrastructure.BearerAuthorizationExtractor
import ecommerce.infrastructure.JWTProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtAuthenticationInterceptor(
    val jwtProvider: JWTProvider,
    private val bearerAuthorizationExtractor: BearerAuthorizationExtractor
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val accessToken = bearerAuthorizationExtractor.extract(request)
        if (accessToken.isBlank()) {
            throw AuthorizationException("Authorization header missing or token is blank")
        }

        jwtProvider.validateToken(accessToken)
        return true
    }
}
