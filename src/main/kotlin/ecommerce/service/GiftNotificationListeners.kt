package ecommerce.service

import ecommerce.event.GiftOrderPlacedEvent
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OrderRepositoryJpa
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Service
class GiftNotificationListeners(
    private val orderRepository: OrderRepositoryJpa,
    private val memberRepository: MemberRepositoryJpa,
    private val emailService: EmailService,
    private val slackService: SlackService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener
    fun handleBuyerConfirmationEmail(event: GiftOrderPlacedEvent) {
        logger.info("--> [LISTENER] Received GiftOrderPlacedEvent for buyer email. Order ID: ${event.orderId}")
        val order = orderRepository.findByIdWithDetails(event.orderId)
        if (order == null) {
            logger.warn("Could not find order with ID ${event.orderId} to send buyer confirmation email.")
            return
        }

        val buyer = memberRepository.findByIdOrNull(order.memberId)
        if (buyer == null) {
            logger.warn("Could not find buyer with ID ${order.memberId} for order ${event.orderId}.")
            return
        }

        logger.info("Sending buyer confirmation email for order ${event.orderId}")
        emailService.sendOrderConfirmation(buyer, order)
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener
    fun handleRecipientNotificationEmail(event: GiftOrderPlacedEvent) {
        logger.info("--> [LISTENER] Received GiftOrderPlacedEvent for recipient email. Order ID: ${event.orderId}")
        val order = orderRepository.findByIdWithDetails(event.orderId)
        if (order == null || !order.isGift || order.giftRecipientEmail.isNullOrBlank()) {
            logger.warn("Order ${event.orderId} is not a valid gift order; skipping recipient notification.")
            return
        }

        val buyer = memberRepository.findByIdOrNull(order.memberId)
        if (buyer == null) {
            logger.warn("Could not find buyer with ID ${order.memberId} for order ${event.orderId}.")
            return
        }

        logger.info("Sending gift recipient notification email for order ${event.orderId}")
        emailService.sendGiftNotification(
            buyer = buyer,
            recipientEmail = order.giftRecipientEmail!!,
            order = order,
            message = order.giftMessage,
        )
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener
    fun handleBuyerSlackNotification(event: GiftOrderPlacedEvent) {
        logger.info("--> [LISTENER] Received GiftOrderPlacedEvent for Slack. Order ID: ${event.orderId}")
        val order = orderRepository.findByIdWithDetails(event.orderId)
        if (order == null) {
            logger.warn("Could not find order with ID ${event.orderId} to send Slack notification.")
            return
        }

        val buyer = memberRepository.findByIdOrNull(order.memberId)
        if (buyer != null && !buyer.slackUserId.isNullOrBlank()) {
            logger.info("Sending Slack notification for order ${event.orderId} to buyer")
            slackService.sendOrderConfirmationSlack(buyer, order)
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener
    fun handleRecipientSlackNotification(event: GiftOrderPlacedEvent) {
        val order = orderRepository.findByIdWithDetails(event.orderId)
        if (order == null || !order.isGift || order.giftRecipientEmail.isNullOrBlank()) {
            return
        }

        val recipient = memberRepository.findByEmail(order.giftRecipientEmail!!)
        if (recipient?.slackUserId.isNullOrBlank()) {
            logger.info("Gift recipient ${order.giftRecipientEmail} is not a registered user with a Slack ID. Skipping Slack notification.")
            return
        }

        val buyer = memberRepository.findByIdOrNull(order.memberId)
        if (buyer == null) {
            logger.warn("Could not find buyer with ID ${order.memberId} for gift order ${event.orderId}.")
            return
        }

        logger.info("Sending Slack gift notification to recipient ${recipient!!.email} for order ${event.orderId}")

        slackService.sendGiftNotificationToRecipient(recipient, buyer, order)
    }
}
