package ecommerce.model

import java.time.LocalDateTime

class CartItemResponseDTO(
    val id: Long,
    val memberId: Long,
    val product: ProductResponseDTO,
    val quantity: Int,
    val addedAt: LocalDateTime,
)
