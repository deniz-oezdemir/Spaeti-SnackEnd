package ecommerce.infrastructure

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.util.Enumeration

@Component
class AuthorizationExtractor {
    fun extractToken(request: HttpServletRequest): String {
        val headerValue = extractAuthorizationHeader(request.getHeaders(AUTHORIZATION)) ?: return ""
        var authHeaderValue = headerValue.substring(BEARER_TYPE.length).trim()
        val commaIndex = authHeaderValue.indexOf(',')
        if (commaIndex > 0) {
            authHeaderValue = authHeaderValue.substring(0, commaIndex)
        }
        return authHeaderValue
    }

    private fun extractAuthorizationHeader(headers: Enumeration<String>): String? {
        while (headers.hasMoreElements()) {
            val value = headers.nextElement()
            if (value.lowercase().startsWith(BEARER_TYPE.lowercase())) {
                return value
            }
        }
        return null
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val BEARER_TYPE = "Bearer"
    }
}
