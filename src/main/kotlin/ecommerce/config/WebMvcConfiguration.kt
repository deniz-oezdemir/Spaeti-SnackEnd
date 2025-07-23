package ecommerce.config

import ecommerce.config.interceptor.CheckLoginInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration(private val checkLogin: CheckLoginInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(checkLogin).addPathPatterns("/**")
            .excludePathPatterns("/api/members/register", "/api/members/login", "/api/me/token")
    }
}