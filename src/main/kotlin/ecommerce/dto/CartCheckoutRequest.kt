package ecommerce.dto

import ecommerce.enums.PaymentMethod
import jakarta.validation.constraints.NotBlank

data class CartCheckoutRequest(
    @field:NotBlank
    val paymentMethod: PaymentMethod,
    val currency: String = "usd",
)
