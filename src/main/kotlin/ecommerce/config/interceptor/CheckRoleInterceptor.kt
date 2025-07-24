package ecommerce.config.interceptor

import ecommerce.annotation.CheckAdminOnly
import ecommerce.entities.Member
import ecommerce.exception.AuthorizationException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class CheckRoleInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod) {
            val method = handler.method
            val isAdminOnly =
                method.isAnnotationPresent(CheckAdminOnly::class.java) ||
                    handler.beanType.isAnnotationPresent(CheckAdminOnly::class.java)
            if (isAdminOnly) {
                val role = request.getAttribute("role") as? Member.Role
                if (role != Member.Role.ADMIN) {
                    throw AuthorizationException("Access denied: admin role required")
                }
            }
        }
        return true
    }
}
