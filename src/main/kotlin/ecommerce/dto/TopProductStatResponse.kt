package ecommerce.dto

import java.time.LocalDateTime

data class TopProductStatResponse(
    val name: String,
    val count: Long,
    val lastAddedAt: LocalDateTime,
)
