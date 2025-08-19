package ecommerce.infrastructure

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class BearerAuthorizationExtractor : AuthorizationExtractor<String> {
    //TODO: actually use the extractor so we can use the header with the keyword "Authentication: Bearer {key}" as currently it works only with "Authentication: {key}"
    override fun extract(request: HttpServletRequest): String {
        val headers = request.getHeaders(HttpHeaders.AUTHORIZATION)

        while (headers.hasMoreElements()) {
            val value = headers.nextElement()
            if (value.startsWith(BEARER_TYPE, ignoreCase = true)) {
                var authHeaderValue = value.substring(BEARER_TYPE.length).trim()

                request.setAttribute(ACCESS_TOKEN_TYPE, BEARER_TYPE)

                val commaIndex = authHeaderValue.indexOf(',')
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex)
                }
                return authHeaderValue
            }
        }
        return ""
    }

    companion object {
        private const val BEARER_TYPE = "Bearer"
        private val ACCESS_TOKEN_TYPE = "${BearerAuthorizationExtractor::class.java.simpleName}.ACCESS_TOKEN_TYPE"
    }
}
