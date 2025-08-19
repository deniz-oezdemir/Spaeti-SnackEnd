package ecommerce.dto

import com.fasterxml.jackson.annotation.JsonProperty

class StripeIntentResponse(
    val id: String,
    val status: String,
    @JsonProperty("client_secret")
    val clientSecret: String? = null,
)
