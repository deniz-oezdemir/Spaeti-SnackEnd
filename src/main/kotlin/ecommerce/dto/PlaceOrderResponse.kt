package ecommerce.dto

class PlaceOrderResponse(
    val orderId: Long?,
    val paymentStatus: String,
    val message: String,
)
