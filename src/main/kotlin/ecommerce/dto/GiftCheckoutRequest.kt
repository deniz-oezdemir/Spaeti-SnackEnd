package ecommerce.dto

import ecommerce.enums.PaymentMethod
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class GiftCheckoutRequest(
    @field:Email @field:NotBlank
    val recipientEmail: String,
    val message: String? = null,
    val currency: String = "eur",
    val paymentMethod: PaymentMethod,
)
