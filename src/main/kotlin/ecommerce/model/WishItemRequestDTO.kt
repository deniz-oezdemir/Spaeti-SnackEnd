package ecommerce.model

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.PositiveOrZero

data class WishItemRequestDTO(
    val productId: Long,
)
