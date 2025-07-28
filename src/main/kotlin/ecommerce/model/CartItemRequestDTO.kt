package ecommerce.model

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.PositiveOrZero

data class CartItemRequestDTO(
    val productId: Long,
    @field:PositiveOrZero(message = ValidationMessages.QUANTITY_NON_NEGATIVE)
    val quantity: Int,
)
