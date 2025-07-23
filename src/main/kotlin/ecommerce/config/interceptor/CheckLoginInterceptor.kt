package ecommerce.config.interceptor

import ecommerce.entities.Member
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
        if (request.method == "GET") return true

        val token = extractToken(request)
        checkAdminCredentials(token)

        return true
    }

    private fun extractToken(request: HttpServletRequest): String {
        val header = request.getHeader("Authorization") ?: throw AuthorizationException("Unauthorized")
        return header.removePrefix("Bearer ").trim()
    }

    private fun checkAdminCredentials(token: String) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw AuthorizationException("Invalid or expired token")
        }

        val (_, role) = jwtTokenProvider.getPayload(token)
        if (role != Member.Role.ADMIN) {
            throw AuthorizationException("Access denied: admin role required")
        }
    }
}
