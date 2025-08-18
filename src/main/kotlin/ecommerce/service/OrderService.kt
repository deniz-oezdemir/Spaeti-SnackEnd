package ecommerce.service

import ecommerce.dto.PaymentRequest
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.dto.StripeIntentResponse
import ecommerce.entity.Member
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Payment
import ecommerce.enums.OrderStatus
import ecommerce.infrastructure.StripeClient
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.OrderItemRepositoryJpa
import ecommerce.repository.OrderRepositoryJpa
import ecommerce.repository.PaymentRepositoryJpa
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class OrderService(
    private val optionRepository: OptionRepositoryJpa,
    private val orderRepository: OrderRepositoryJpa,
    private val orderItemRepository: OrderItemRepositoryJpa,
    private val paymentRepository: PaymentRepositoryJpa,
    private val cartItemRepository: CartItemRepositoryJpa,
    private val cartRepository: CartRepositoryJpa,
    private val stripeClient: StripeClient,
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    fun place(
        member: Member,
        req: PlaceOrderRequest,
    ): PlaceOrderResponse {
        try {
            val option =
                optionRepository.findById(req.optionId)
                    .orElseThrow { IllegalArgumentException("Option not found: ${req.optionId}") }
            require(req.quantity > 0) { "Quantity must be positive" }
            require(option.quantity >= req.quantity) {
                "Insufficient stock. Available=${option.quantity}, requested=${req.quantity}"
            }

            val amountMinor = toMinorUnits(option.product.price, req.quantity)

            val stripeRes =
                stripeClient.createAndConfirmPayment(
                    PaymentRequest(amountMinor, req.currency, req.paymentMethod),
                )
            if (stripeRes.status != "succeeded") {
                throw IllegalArgumentException("Payment not approved (status=${stripeRes.status}).")
            }

            val order = persistAfterStripeSuccess(member, option.id!!, req.quantity, amountMinor, req.currency, stripeRes)

            handleSuccessfulOrderNotification(member, order)

            return PlaceOrderResponse(order.id, stripeRes.status, "Order placed and paid successfully.")
        } catch (e: Exception) {
            logger.error("Order processing failed for member ${member.id}: ${e.message}", e)

            handleFailedOrderNotification(member, e)

            throw e // TODO: create custom exception
        }
    }

    private fun handleSuccessfulOrderNotification(member: Member, order: Order) {
        try {
            emailService.sendOrderConfirmation(member, order)
        } catch (e: Exception) {
            logger.error("Failed to send confirmation email for order ${order.id}", e)
        }
    }

    private fun handleFailedOrderNotification(member: Member, exception: Exception) {
        try {
            emailService.sendOrderFailureNotification(member, exception.message ?: "An unknown error occurred.")
        } catch (emailEx: Exception) {
            logger.error("Failed to send failure notification email for member ${member.id}", emailEx)
        }
    }

    @Transactional
    protected fun persistAfterStripeSuccess(
        member: Member,
        optionId: Long,
        requestedQty: Long,
        amountMinor: Long,
        currency: String,
        stripeRes: StripeIntentResponse,
    ): Order {
        val option =
            optionRepository.findById(optionId)
                .orElseThrow { IllegalArgumentException("Option not found during persist: $optionId") }
        require(option.quantity >= requestedQty) { "Insufficient stock during persist." }

        val order =
            orderRepository.save(
                Order(
                    memberId = member.id ?: error("Member must be persisted (id is null)"),
                    status = OrderStatus.PAID,
                    orderDateTime = LocalDateTime.now(),
                ),
            )

        orderItemRepository.save(
            OrderItem(
                order = order,
                productOption = option,
                quantity = requestedQty.toInt(),
                price = option.product.price,
            ),
        )

        paymentRepository.save(
            Payment(
                order = order,
                amount = amountMinor,
                currency = currency,
                status = "PAID",
                stripeSessionId = stripeRes.id,
                paymentMethod = "stripe",
            ),
        )

        option.quantity -= requestedQty
        optionRepository.save(option)

        // Remove from cart if present
        cartRepository.findByMemberId(member.id!!)?.let { cart ->
            cartItemRepository.findByCartIdAndProductOptionId(cart.id!!, option.id!!)?.let {
                cartItemRepository.delete(it)
            }
        }

        return order
    }

    private fun toMinorUnits(
        unitPrice: Double,
        qty: Long,
    ): Long =
        BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(qty))
            .multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValueExact()
}
