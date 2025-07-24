package ecommerce.entities

import java.time.LocalDateTime

data class CartItem(
    val id: Long? = null,
    val memberId: Long,
    val productId: Long,
    val quantity: Int,
    val addedAt: LocalDateTime? = null,
)
