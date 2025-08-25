package ecommerce

import ecommerce.config.StripeProperties
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@OpenAPIDefinition(
    info =
        Info(
            title = "Spaeti SnackEnd API",
            version = "v0.1",
            description = "API documentation for the Spaeti SnackEnd e-commerce application.",
        ),
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
)
@EnableAsync
@EnableConfigurationProperties(StripeProperties::class)
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
