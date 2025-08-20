package ecommerce.dto

class PaymentRequest(
    val amount: Long,
    val currency: String,
    val paymentMethod: String,
)
