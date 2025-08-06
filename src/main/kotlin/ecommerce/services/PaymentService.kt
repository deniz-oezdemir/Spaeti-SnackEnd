package ecommerce.services

import ecommerce.exception.NotFoundException
import ecommerce.exception.PaymentFailedException
import ecommerce.infrastructure.StripeClient
import ecommerce.model.MemberDTO
import ecommerce.model.PaymentRequestDTO
import ecommerce.model.StripePaymentRequest
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.OptionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val optionRepository: OptionRepository,
    private val cartItemRepository: CartItemRepository,
    private val stripeClient: StripeClient
) {

    @Transactional
    fun processPayment(req: PaymentRequestDTO, member: MemberDTO) {
        val option = optionRepository.findByIdWithLock(req.optionId).orElseThrow { NotFoundException("Product option with id=${req.optionId} not found") }

        // Check stock to not charge customer in case of insufficient stock
        option.subtract(req.quantity)

        val amountInCents = (option.product!!.price * req.quantity * 100).toLong()
        val stripeRequest = StripePaymentRequest(
            amountInCents, "eur", req.paymentMethod
        )

        try {
            stripeClient.createPaymentIntent(stripeRequest)
        } catch (e: IllegalArgumentException) {
            throw PaymentFailedException(e.message ?: "Payment failed due to an unknown error.")
        }

        // Save updated stock
        optionRepository.save(option)
        cartItemRepository.deleteByProductIdAndMemberId(option.product!!.id!!, member.id!!)
    }


}
