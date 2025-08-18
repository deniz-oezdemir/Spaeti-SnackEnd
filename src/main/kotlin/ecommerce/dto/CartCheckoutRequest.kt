package ecommerce.dto

import jakarta.validation.constraints.NotBlank

data class CartCheckoutRequest(
    @field:NotBlank
    val paymentMethod: String,
    val currency: String = "usd",
)
