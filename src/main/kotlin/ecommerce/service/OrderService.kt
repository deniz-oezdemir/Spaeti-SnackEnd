package ecommerce.service

import ecommerce.dto.CartCheckoutRequest
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.dto.StripeIntentResponse
import ecommerce.entity.CartItem
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
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
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
    private val emailService: EmailService,
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional
    fun checkoutCart(
        member: Member,
        req: CartCheckoutRequest,
    ): PlaceOrderResponse {
        val cart =
            cartRepository.findByMemberId(member.id!!)
                ?: throw IllegalStateException("User does not have a cart.")

        // We need to fetch the full CartItem objects to get quantities and options
        val cartItems = cartItemRepository.findAllByCartId(cart.id!!, Pageable.unpaged()).content
        if (cartItems.isEmpty()) {
            throw IllegalStateException("Cannot checkout an empty cart.")
        }

        try {
            // 1. Calculate the grand total from all cart items
            val grandTotal = cartItems.sumOf { it.productOption.product.price * it.quantity }
            val amountMinor = toMinorUnits(grandTotal, 1) // Use quantity 1 since it's the total

            // 2. Create a single Stripe payment for the grand total
            val stripeRes =
                stripeClient.createAndConfirmPayment(
                    PaymentRequest(amountMinor, req.currency, req.paymentMethod),
                )
            if (stripeRes.status != "succeeded") {
                throw IllegalArgumentException("Payment not approved (status=${stripeRes.status}).")
            }

            // 3. Persist the multi-item order and clear the cart
            val order = persistCartOrderAfterStripeSuccess(member, cartItems, amountMinor, req.currency, stripeRes)
            handleSuccessfulOrderNotification(member, order)

            return PlaceOrderResponse(order.id, stripeRes.status, "Cart checkout successful.")
        } catch (e: Exception) {
            handleFailedOrderNotification(member, e)
            throw e
        }
    }

    @Transactional
    protected fun persistCartOrderAfterStripeSuccess(
        member: Member,
        cartItems: List<CartItem>,
        amountMinor: Long,
        currency: String,
        stripeRes: StripeIntentResponse,
    ): Order {
        // Create one order
        val order =
            orderRepository.save(
                Order(memberId = member.id!!, status = OrderStatus.PAID),
            )

        // Create an OrderItem for each CartItem
        val orderItems =
            cartItems.map { cartItem ->
                OrderItem(
                    order = order,
                    productOption = cartItem.productOption,
                    quantity = cartItem.quantity.toInt(),
                    price = cartItem.productOption.product.price,
                )
            }
        orderItemRepository.saveAll(orderItems)

        order.items.addAll(orderItems)

        // Decrease stock for each item
        cartItems.forEach { cartItem ->
            val option =
                optionRepository.findWithLockById(cartItem.productOption.id!!)
                    ?: throw NoSuchElementException("Option not found during stock update")
            option.decreaseQuantity(cartItem.quantity)
            optionRepository.save(option)
        }

        // Save the payment record
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

        // Clear the user's cart
        cartItemRepository.deleteAll(cartItems)

        return order
    }

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

    private fun handleSuccessfulOrderNotification(
        member: Member,
        order: Order,
    ) {
        try {
            emailService.sendOrderConfirmation(member, order)
        } catch (e: Exception) {
            logger.error("Failed to send confirmation email for order ${order.id}", e)
        }
    }

    private fun handleFailedOrderNotification(
        member: Member,
        exception: Exception,
    ) {
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
