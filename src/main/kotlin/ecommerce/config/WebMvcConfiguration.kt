package ecommerce.config

import ecommerce.config.interceptor.CheckLoginInterceptor
import ecommerce.config.interceptor.CheckRoleInterceptor
import ecommerce.config.resolvers.LoginMemberArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration(
    private val checkLogin: CheckLoginInterceptor,
    private val checkRole: CheckRoleInterceptor,
    private val loginMemberArgumentResolver: LoginMemberArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(checkLogin).addPathPatterns("/api/**", "/products", "/admin/**")
            .excludePathPatterns("/api/members/**")
        registry.addInterceptor(checkRole).addPathPatterns("/admin/**", "/api/products/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loginMemberArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*") // TODO: replace with specific domain of my frontend for production
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)
    }
}
