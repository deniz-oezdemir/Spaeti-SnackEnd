package ecommerce.interceptor

import ecommerce.model.UserRole
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
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")?.trim() ?: return false
        val member =
            try {
                authService.findMemberByToken(token)
            } catch (_: Exception) {
                null
            }

        val uri = request.requestURI
        if (uri.startsWith("/api/protected/admin") && member?.role != UserRole.ADMIN.name) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
        }

        return true
    }
}
