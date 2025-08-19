package ecommerce.dto

import ecommerce.enums.PaymentMethod

class PaymentRequest(
    val amount: Long,
    val currency: String,
    val paymentMethod: PaymentMethod,
)
