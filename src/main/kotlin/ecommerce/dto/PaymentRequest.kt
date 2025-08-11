package ecommerce.dto

data class PaymentRequest(
    val amount: Long,
    val currency: String,
    val paymentMethod: String,
)
