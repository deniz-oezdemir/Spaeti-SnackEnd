package ecommerce.model

import java.time.LocalDateTime

data class TopProductDTO(
    val name: String,
    val count: Int,
    val mostRecentAddedAt: LocalDateTime,
)
