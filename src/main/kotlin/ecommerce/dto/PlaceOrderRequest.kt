package ecommerce.dto

import ecommerce.enums.PaymentMethod
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class PlaceOrderRequest(
    @field:NotNull
    val optionId: Long,
    @field:Min(1)
    val quantity: Long,
    @field:NotNull
    val paymentMethod: PaymentMethod,
    val currency: String = "usd",
)
