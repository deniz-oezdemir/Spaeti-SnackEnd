package ecommerce.service

import ecommerce.dto.CartCheckoutRequest
import ecommerce.dto.GiftCheckoutRequest
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.entity.Member
import ecommerce.entity.Order
import ecommerce.infrastructure.StripeClient
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.util.MoneyUtil.toMinorUnits
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val memberRepository: MemberRepositoryJpa,
    private val optionRepository: OptionRepositoryJpa,
    private val cartItemRepository: CartItemRepositoryJpa,
    private val cartRepository: CartRepositoryJpa,
    private val stripeClient: StripeClient,
    private val emailService: EmailService,
    private val orderPersistenceService: OrderPersistenceService,
    private val slackService: SlackService,
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
                    PaymentRequest(amountMinor, req.currency, req.paymentMethod.id),
                )
            if (stripeRes.status != "succeeded") {
                throw IllegalArgumentException("Payment not approved (status=${stripeRes.status}).")
            }

            // 3. Persist the multi-item order and clear the cart
            val order =
                orderPersistenceService.persistCartOrderAfterStripeSuccess(
                    member,
                    cartItems,
                    amountMinor,
                    req.currency,
                    stripeRes,
                    req.paymentMethod,
                )
            handleSuccessfulOrderNotification(member, order)

            return PlaceOrderResponse(order.id, stripeRes.status, "Cart checkout successful.")
        } catch (e: Exception) {
            handleFailedOrderNotification(member, e)
            throw e
        }
    }

    @Transactional
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
                    PaymentRequest(amountMinor, req.currency, req.paymentMethod.id),
                )
            if (stripeRes.status != "succeeded") {
                throw IllegalArgumentException("Payment not approved (status=${stripeRes.status}).")
            }

            val order =
                orderPersistenceService.persistAfterStripeSuccess(
                    member,
                    option.id!!,
                    req.quantity,
                    amountMinor,
                    req.currency,
                    stripeRes,
                    req.paymentMethod,
                )

                handleSuccessfulOrderNotification(member, order)

            return PlaceOrderResponse(order.id, stripeRes.status, "Order placed and paid successfully.")
        } catch (e: Exception) {
            logger.error("Order processing failed for member ${member.id}: ${e.message}", e)

            handleFailedOrderNotification(member, e)

            throw e // TODO: create custom exception
        }
    }

    @Transactional
    fun placeGift(
        member: Member,
        req: GiftCheckoutRequest,
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
            // 1) Compute grand total (same idea as checkoutCart)
            val grandTotal = cartItems.sumOf { it.productOption.product.price * it.quantity }
            val amountMinor = toMinorUnits(grandTotal, 1) // Use quantity 1 since it's the total

            // 2) Stripe payment
            val stripeRes =
                stripeClient.createAndConfirmPayment(
                    PaymentRequest(amountMinor, req.currency, req.paymentMethod.id),
                )
            if (stripeRes.status != "succeeded") {
                throw IllegalArgumentException("Payment not approved (status=${stripeRes.status}).")
            }

            // 3) Persist the order (parallel to persistCartOrderAfterStripeSuccess)
            val order =
                orderPersistenceService.persistGiftOrderAfterStripeSuccess(
                    buyer = member,
                    cartItems = cartItems,
                    recipientEmail = req.recipientEmail,
                    message = req.message,
                    amountMinor = amountMinor,
                    currency = req.currency,
                    stripeRes = stripeRes,
                    paymentMethod = req.paymentMethod,
                )

            // 4) Emails
            handleSuccessfulOrderNotification(member, order) // buyer confirmation (can include totals)
            emailService.sendGiftNotification(
                buyer = member,
                recipientEmail = req.recipientEmail,
                order = order,
                message = req.message,
            )

            return PlaceOrderResponse(order.id, stripeRes.status, "Gift order successful.")
        } catch (e: Exception) {
            handleFailedOrderNotification(member, e)
            throw e
        }
    }

    private fun handleSuccessfulOrderNotification(
        member: Member,
        order: Order,
    ) {
        // Send Email
        try {
            emailService.sendOrderConfirmation(member, order)
        } catch (e: Exception) {
            logger.error("Failed to send confirmation email for order ${order.id}", e)
        }
        // Send Slack
        try {
            if (member.slackUserId != null) {
                slackService.sendOrderConfirmationSlack(member, order)
            }
        } catch (e: Exception) {
            logger.error("Failed to send Slack DM for order ${order.id}", e)
        }
    }

    private fun handleFailedOrderNotification(
        member: Member,
        exception: Exception,
    ) {
        val reason = exception.message ?: "An unknown error occurred."
        // Send Email
        try {
            emailService.sendOrderFailureNotification(member, reason)
        } catch (emailEx: Exception) {
            logger.error("Failed to send failure notification email for member ${member.id}", emailEx)
        }
        // Send Slack Message
        if (member.slackUserId != null) {
            try {
                slackService.sendOrderFailureSlack(member, reason)
            } catch (slackEx: Exception) {
                logger.error("Failed to send failure notification email for member ${member.id}", slackEx)
            }
        }
    }
}
