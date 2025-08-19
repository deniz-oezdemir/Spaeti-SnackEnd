package ecommerce.service

import ecommerce.dto.PaymentRequest
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.entity.Member
import ecommerce.infrastructure.StripeClient
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.OrderItemRepositoryJpa
import ecommerce.repository.OrderRepositoryJpa
import ecommerce.repository.PaymentRepositoryJpa
import ecommerce.util.MoneyUtil
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val optionRepository: OptionRepositoryJpa,
    private val orderRepository: OrderRepositoryJpa,
    private val orderItemRepository: OrderItemRepositoryJpa,
    private val paymentRepository: PaymentRepositoryJpa,
    private val cartItemRepository: CartItemRepositoryJpa,
    private val cartRepository: CartRepositoryJpa,
    private val stripeClient: StripeClient,
    private val orderPersistenceService: OrderPersistenceService,
) {
    fun place(
        member: Member,
        req: PlaceOrderRequest,
    ): PlaceOrderResponse {
        val option =
            optionRepository.findById(req.optionId)
                .orElseThrow { IllegalArgumentException("Option not found: ${req.optionId}") }
        require(req.quantity > 0) { "Quantity must be positive" }
        require(option.quantity >= req.quantity) {
            "Insufficient stock. Available=${option.quantity}, requested=${req.quantity}"
        }

        val amountMinor = MoneyUtil.toMinorUnits(option.product.price, req.quantity)
        val stripeRes =
            stripeClient.createAndConfirmPayment(
                PaymentRequest(amountMinor, req.currency, req.paymentMethod),
            )
        if (stripeRes.status != "succeeded") {
            throw IllegalArgumentException("Payment not approved (status=${stripeRes.status}).")
        }

        val orderId =
            orderPersistenceService.persistAfterStripeSuccess(
                member,
                option.id!!,
                req.quantity,
                amountMinor,
                req.currency,
                stripeRes,
                req.paymentMethod,
            )
        return PlaceOrderResponse(orderId, stripeRes.status, "Order placed and paid successfully.")
    }
}
