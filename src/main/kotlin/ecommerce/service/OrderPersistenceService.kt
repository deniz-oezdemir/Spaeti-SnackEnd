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
import java.math.BigDecimal
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
            optionRepository.findWithLockById(optionId)
                ?: throw NoSuchElementException("Option not found id=$optionId")
        require(option.quantity >= requestedQty) { "Insufficient stock during persist." }

        val order =
            orderRepository.save(
                Order(
                    memberId = member.id ?: error("Member must be persisted (id is null)"),
                    status = OrderStatus.PAID,
                    orderDateTime = LocalDateTime.now(),
                ),
            )

        val orderItem =
            OrderItem(
                order = order,
                productOption = option,
                quantity = requestedQty.toInt(),
                price = option.product.price,
                productName = option.product.name,
                optionName = option.name,
                productImageUrl = option.product.imageUrl,
            )
        orderItemRepository.save(orderItem)
        order.items.add(orderItem)

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
        val totalAmount = BigDecimal.valueOf(amountMinor, 2)
        val order =
            orderRepository.save(
                Order(memberId = member.id!!, status = OrderStatus.PAID, totalAmount = totalAmount),
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

    @Transactional
    fun persistGiftOrderAfterStripeSuccess(
        buyer: Member,
        cartItems: List<CartItem>,
        recipientEmail: String,
        message: String?,
        amountMinor: Long,
        currency: String,
        stripeRes: StripeIntentResponse,
        paymentMethod: PaymentMethod,
    ): Order {
        // Order with gift metadata
        val order =
            Order(
                memberId = buyer.id!!,
                status = OrderStatus.PAID,
                isGift = true,
                giftRecipientEmail = recipientEmail,
                giftMessage = message,
                totalAmount = BigDecimal.valueOf(amountMinor, 2),
            )
        val saved = orderRepository.save(order)

        // One item per option (qty = 1)
        val items =
            cartItems.map { cartItem ->
                OrderItem(
                    order = saved,
                    productOption = cartItem.productOption,
                    quantity = cartItem.quantity.toInt(),
                    price = cartItem.productOption.product.price,
                    productName = cartItem.productOption.product.name,
                    optionName = cartItem.productOption.name,
                    productImageUrl = cartItem.productOption.product.imageUrl,
                )
            }
        orderItemRepository.saveAll(items)

        saved.items.addAll(items)

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

        return saved
    }
}
