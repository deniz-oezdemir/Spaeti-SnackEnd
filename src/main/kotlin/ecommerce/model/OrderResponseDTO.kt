package ecommerce.model

import ecommerce.entities.Order
import java.time.LocalDateTime

data class OrderResponseDTO(
    val orderId: Long,
    val orderDate: LocalDateTime,
    val orderStatus: Order.OrderStatus,
    val totalAmount: Double,
    val stripePaymentId: String,
    val items: List<OrderItemDTO>,
)

data class OrderItemDTO(
    val productName: String,
    val optionName: String,
    val price: Double,
    val quantity: Long,
)
