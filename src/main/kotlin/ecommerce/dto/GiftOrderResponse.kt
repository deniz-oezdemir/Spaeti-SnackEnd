package ecommerce.dto

import java.math.BigDecimal

data class GiftOrderResponse(
    val orderId: Long,
    val status: String,
    val recipientEmail: String,
    val totalAmount: BigDecimal,
    val itemCount: Int,
)
