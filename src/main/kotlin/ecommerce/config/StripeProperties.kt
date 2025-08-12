package ecommerce.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stripe")
data class StripeProperties(
    val secretKey: String = "placeholder_key" // TODO: change back to real key or do not use stripe
)
