package ecommerce.dto

import ecommerce.enums.PaymentMethod
import jakarta.validation.constraints.NotNull

data class CartCheckoutRequest(
    @field:NotNull
    val paymentMethod: PaymentMethod,
    val currency: String = "usd",
)
