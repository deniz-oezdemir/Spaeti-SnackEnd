package ecommerce.model

import java.time.LocalDateTime

class WishItemResponseDTO(
    val id: Long,
    val memberId: Long,
    val product: ProductResponseDTO,
    val addedAt: LocalDateTime,
)
