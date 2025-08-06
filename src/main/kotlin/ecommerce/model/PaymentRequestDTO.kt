package ecommerce.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class PaymentRequestDTO(
    @field:Positive
    val optionId: Long,

    @field:Positive
    val quantity: Long,

    @field:NotBlank
    val paymentMethod: String
)

