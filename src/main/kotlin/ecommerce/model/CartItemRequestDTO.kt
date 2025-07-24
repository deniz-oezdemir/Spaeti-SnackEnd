package ecommerce.model

import jakarta.validation.constraints.PositiveOrZero

data class CartItemRequestDTO(
    val productId: Long,
    @field:PositiveOrZero
    val quantity: Int,
)
