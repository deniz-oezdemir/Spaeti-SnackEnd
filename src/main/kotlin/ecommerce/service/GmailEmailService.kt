package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Order
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class GmailEmailService(
    private val mailSender: JavaMailSender
) : EmailService {

    override fun sendOrderConfirmation(member: Member, order: Order) {
        val message = SimpleMailMessage().apply {
            setTo(member.email)
            setSubject("✅ Your Order #${order.id} is Confirmed!")
            setText("""
                Hi ${member.name},

                Thank you for your purchase! We've successfully received your order.

                Order ID: ${order.id}
                Order Date: ${order.orderDateTime}
                Status: ${order.status}

                We'll notify you again once your items have shipped.

                Thanks,
                Spaeti-SnackEnd
            """.trimIndent())
        }
        
        mailSender.send(message)
    }

    override fun sendOrderFailureNotification(member: Member, reason: String) {
        val message = SimpleMailMessage().apply {
            setTo(member.email)
            setSubject("❌ There was an issue with your order")
            setText("""
                Hi ${member.name},

                We're sorry, but we were unable to process your recent order.

                Reason: $reason

                Please check your payment details and try again. If the problem persists, please contact our support team.

                Thanks,
                Spaeti-SnackEnd
            """.trimIndent())
        }
        
        mailSender.send(message)
    }
}
