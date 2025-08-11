package ecommerce.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PlaceOrderRequest(
    @field:NotNull
    val optionId: Long,
    @field:Min(1)
    val quantity: Long,
    @field:NotBlank
    val paymentMethod: String,
    val currency: String = "usd",
)
