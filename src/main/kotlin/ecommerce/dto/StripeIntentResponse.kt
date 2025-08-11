package ecommerce.dto

data class StripeIntentResponse(
    val id: String,
    val status: String,
    val client_secret: String? = null,
)
