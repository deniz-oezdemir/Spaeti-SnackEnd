package ecommerce.dto

import ecommerce.enums.PaymentMethod
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class GiftCheckoutRequest(
    @field:Email @field:NotBlank
    val recipientEmail: String,

    // These are OPTION IDs (since OrderItem uses productOption)
    @field:NotEmpty
    val productOptionIds: List<Long>,

    val message: String? = null,

    // same shape as CartCheckoutRequest
    val currency: String = "usd",
    val paymentMethod: PaymentMethod, // has `.id` like in your cart flow
)
