package ecommerce.resolver

import ecommerce.annotations.LoginMember
import ecommerce.handler.AuthorizationException
import ecommerce.service.AuthService
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginMemberArgumentResolver(
    private val authService: AuthService,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginMember::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val token =
            webRequest.getHeader("Authorization")?.removePrefix("Bearer ")
                ?: throw AuthorizationException("Missing Authorization token")

        val memberResponse = authService.findMemberByToken(token)
        return memberResponse
    }
}
