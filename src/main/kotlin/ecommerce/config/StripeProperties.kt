package ecommerce.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stripe")
class StripeProperties(
    val secret: String,
)
