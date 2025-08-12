package ecommerce.model

import ecommerce.entities.Order
import java.time.LocalDateTime

class OrderResponseDTO(
    val orderId: Long,
    val orderDate: LocalDateTime,
    val orderStatus: Order.OrderStatus,
    val totalAmount: Double,
    val stripePaymentId: String,
    val items: List<OrderItemDTO>,
)

class OrderItemDTO(
    val productName: String,
    val optionName: String,
    val price: Double,
    val quantity: Long,
)
