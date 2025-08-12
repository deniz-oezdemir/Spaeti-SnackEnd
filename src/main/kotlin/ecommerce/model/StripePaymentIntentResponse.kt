package ecommerce.model

import com.fasterxml.jackson.annotation.JsonProperty

class StripePaymentIntentResponse(
    @JsonProperty("id")
    val id: String,
)
