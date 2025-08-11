package ecommerce.dto

data class PlaceOrderResponse(
    val orderId: Long?,
    val paymentStatus: String,
    val message: String,
)
