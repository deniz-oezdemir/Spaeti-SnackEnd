package ecommerce.mappers

import ecommerce.entities.Order
import ecommerce.entities.OrderItem
import ecommerce.model.OrderItemDTO
import ecommerce.model.OrderResponseDTO

fun OrderItem.toDTO(): OrderItemDTO =
    OrderItemDTO(
        productName = this.productName,
        optionName = this.optionName,
        price = this.price,
        quantity = this.quantity,
    )

fun Order.toDTO(): OrderResponseDTO =
    OrderResponseDTO(
        orderId = this.id!!,
        orderDate = this.orderDate,
        orderStatus = this.status,
        totalAmount = this.payment.amount / 100.0,
        stripePaymentId = this.payment.stripePaymentId,
        items = this.items.map { it.toDTO() },
    )
