package ecommerce.services

import ecommerce.entities.Order
import ecommerce.entities.OrderItem
import ecommerce.entities.Payment
import ecommerce.exception.NotFoundException
import ecommerce.exception.PaymentFailedException
import ecommerce.infrastructure.StripeClient
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.model.MemberDTO
import ecommerce.model.OrderResponseDTO
import ecommerce.model.PaymentRequestDTO
import ecommerce.model.StripePaymentRequest
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService(
    private val optionRepository: OptionRepository,
    private val cartItemRepository: CartItemRepository,
    private val stripeClient: StripeClient,
    private val orderRepository: OrderRepository,
) {
    @Transactional
    fun placeOrder(
        req: PaymentRequestDTO,
        member: MemberDTO,
    ): Order {
        val option =
            optionRepository.findByIdWithLock(req.optionId).orElseThrow {
                NotFoundException("Product option with id=${req.optionId} not found")
            }

        // Check stock to not charge customer in case of insufficient stock
        option.subtract(req.quantity)

        val amountInCents = (option.product!!.price * req.quantity * 100).toLong()
        val stripeRequest =
            StripePaymentRequest(
                amountInCents,
                "eur",
                req.paymentMethod,
            )

        val stripeResponse =
            try {
                stripeClient.createPaymentIntent(stripeRequest)
            } catch (e: IllegalArgumentException) {
                throw PaymentFailedException(e.message ?: "Payment failed due to an unknown error.")
            }

        val payment =
            Payment(
                amount = amountInCents,
                stripePaymentId = stripeResponse?.id ?: "pi_error_id_not_found",
            )

        val order =
            Order(
                member = member.toEntity(),
                payment = payment,
                orderDate = LocalDateTime.now(),
                status = Order.OrderStatus.COMPLETED,
            )

        val orderItem =
            OrderItem(
                productName = option.product!!.name,
                optionName = option.name,
                price = option.product!!.price,
                quantity = req.quantity,
            )

        order.addOrderItem(orderItem)

        val savedOrder = orderRepository.save(order)

        optionRepository.save(option)
        cartItemRepository.deleteByProductIdAndMemberId(option.product!!.id!!, member.id!!)

        return savedOrder
    }

    @Transactional(readOnly = true)
    fun findOrdersByMemberId(memberId: Long): List<OrderResponseDTO> {
        val orders = orderRepository.findByMemberId(memberId)
        return orders.map { it.toDTO() }
    }
}
