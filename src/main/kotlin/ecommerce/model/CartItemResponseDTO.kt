package ecommerce.model

import java.time.LocalDateTime

data class CartItemResponseDTO(
    val id: Long,
    val memberId: Long,
    val product: ProductResponseDTO,
    val quantity: Int,
    val addedAt: LocalDateTime,
)
