package ecommerce.model

data class StripePaymentRequest(
    val amount: Long,
    val currency: String,
    val paymentMethod: String,
)
