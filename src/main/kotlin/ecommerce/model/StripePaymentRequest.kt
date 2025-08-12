package ecommerce.model

class StripePaymentRequest(
    val amount: Long,
    val currency: String,
    val paymentMethod: String,
)
