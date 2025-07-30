package ecommerce.model

import java.time.LocalDateTime

data class WishItemResponseDTO(
    val id: Long,
    val memberId: Long,
    val product: ProductDTO,
    val addedAt: LocalDateTime,
)
