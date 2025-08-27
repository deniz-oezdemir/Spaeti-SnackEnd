package ecommerce.interceptor

import ecommerce.enums.UserRole
import ecommerce.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminRoleCheckInterceptor(
    private val authService: AuthService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val uri = request.requestURI
        if (!uri.startsWith("/api/protected/admin")) {
            return true
        }

        // Extract the token the same way as the JWT interceptor
        val tokenHeader = request.getHeader("Authorization") ?: ""
        val token = if (tokenHeader.startsWith("Bearer ")) tokenHeader.removePrefix("Bearer ").trim() else tokenHeader.trim()

        // Missing token → 401
        if (token.isBlank()) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
        }

        // Try to resolve member from the token
        val member =
            try {
                authService.findMemberByToken(token)
            } catch (_: Exception) {
                null
            }

        // Unknown/invalid token → 401
        if (member == null) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
        }

        // Authenticated but not admin → 403
        if (member.role != UserRole.ADMIN.name) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            return false
        }

        // Admin → OK
        return true
    }
}
