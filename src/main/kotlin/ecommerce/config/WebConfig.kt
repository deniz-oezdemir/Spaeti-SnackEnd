package ecommerce.config

import ecommerce.interceptor.AdminRoleCheckInterceptor
import ecommerce.interceptor.JwtAuthenticationInterceptor
import ecommerce.resolver.LoginMemberArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val jwtAuthenticationInterceptor: JwtAuthenticationInterceptor,
    private val loginMemberArgumentResolver: LoginMemberArgumentResolver,
    private val adminRoleCheckInterceptor: AdminRoleCheckInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
            .addPathPatterns("/api/protected/**")
        registry.addInterceptor(jwtAuthenticationInterceptor)
            .addPathPatterns("/auth/find-member")
        registry.addInterceptor(adminRoleCheckInterceptor)
            .addPathPatterns("/api/protected/admin/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loginMemberArgumentResolver)
    }
}
