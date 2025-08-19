package ecommerce.service

import ecommerce.dto.StripeIntentResponse
import ecommerce.entity.CartItem
import ecommerce.entity.Member
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Payment
import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentMethod
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.OrderItemRepositoryJpa
import ecommerce.repository.OrderRepositoryJpa
import ecommerce.repository.PaymentRepositoryJpa
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.collections.forEach

@Service
class OrderPersistenceService(
    private val optionRepository: OptionRepositoryJpa,
    private val orderRepository: OrderRepositoryJpa,
    private val orderItemRepository: OrderItemRepositoryJpa,
    private val paymentRepository: PaymentRepositoryJpa,
    private val cartItemRepository: CartItemRepositoryJpa,
    private val cartRepository: CartRepositoryJpa,
) {
    @Transactional
    fun persistAfterStripeSuccess(
        member: Member,
        optionId: Long,
        requestedQty: Long,
        amountMinor: Long,
        currency: String,
        stripeRes: StripeIntentResponse,
        paymentMethod: PaymentMethod,
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
                productName = option.product.name,
                optionName = option.name,
                productImageUrl = option.product.imageUrl,
            ),
        )

        paymentRepository.save(
            Payment(
                order = order,
                amount = amountMinor,
                currency = currency,
                status = "PAID",
                stripeSessionId = stripeRes.id,
                paymentMethod = paymentMethod,
            ),
        )

        option.decreaseQuantity(requestedQty)
        optionRepository.save(option)

        cartRepository.findByMemberId(member.id!!)?.let { cart ->
            cartItemRepository.findByCartIdAndProductOptionId(cart.id!!, option.id!!)?.let {
                cartItemRepository.delete(it)
            }
        }

        return order
    }

    @Transactional
    fun persistCartOrderAfterStripeSuccess(
        member: Member,
        cartItems: List<CartItem>,
        amountMinor: Long,
        currency: String,
        stripeRes: StripeIntentResponse,
        paymentMethod: PaymentMethod,
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
                    productName = cartItem.productOption.product.name,
                    optionName = cartItem.productOption.name,
                    productImageUrl = cartItem.productOption.product.imageUrl,
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
                paymentMethod = paymentMethod,
            ),
        )

        // Clear the user's cart
        cartItemRepository.deleteAll(cartItems)

        return order
    }
}
