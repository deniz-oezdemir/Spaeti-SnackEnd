package ecommerce.model

import com.fasterxml.jackson.annotation.JsonProperty

data class StripePaymentIntentResponse(
    @JsonProperty("id")
    val id: String,
)
