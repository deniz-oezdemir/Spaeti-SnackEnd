package ecommerce.model

import jakarta.validation.constraints.PositiveOrZero
import ecommerce.util.ValidationMessages

data class CartItemRequestDTO(
    val productId: Long,
    @field:PositiveOrZero(message = ValidationMessages.QUANTITY_NON_NEGATIVE)
    val quantity: Int,
)
