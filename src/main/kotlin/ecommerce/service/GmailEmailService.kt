package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Order
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class GmailEmailService(
    private val mailSender: JavaMailSender,
) : EmailService {
    override fun sendOrderConfirmation(
        member: Member,
        order: Order,
    ) {
        val itemsListString =
            order.items.joinToString("\n") { item ->
                val productName = item.productOption.product.name
                val optionName = item.productOption.name
                val quantity = item.quantity
                "- $quantity x $productName ($optionName)"
            }

        val message: SimpleMailMessage

        if (order.isGift) {
            message =
                SimpleMailMessage().apply {
                    setTo(member.email)
                    setSubject("🎁 Your gift purchase is confirmed!")
                    setText(
                        """
    |Hi ${member.name},
    |
    |Thank you for your purchase! We've successfully sent your gift to ${order.giftRecipientEmail}.
    |
    |Order Date: ${order.orderDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
    |Status: ${order.status}
    |
    |Gifts Purchased:
    |$itemsListString
    |
    |Total Amount: EUR ${String.format("%.2f", order.totalAmount)}
    |
    |Thanks,
    |Spaeti-SnackEnd
                        """.trimMargin(),
                    )
                }
        } else {
            message =
                SimpleMailMessage().apply {
                    setTo(member.email)
                    setSubject("✅ Your Order is Confirmed!")
                    setText(
                        """
    |Hi ${member.name},
    |
    |Thank you for your purchase! We've successfully received your order.
    |
    |Order Date: ${order.orderDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
    |Status: ${order.status}
    |
    |Items Purchased:
    |$itemsListString
    |
    |Total Amount: EUR ${String.format("%.2f", order.totalAmount)}
    |
    |Thanks,
    |Spaeti-SnackEnd
                        """.trimMargin(),
                    )
                }
        }

        mailSender.send(message)
    }

    override fun sendOrderFailureNotification(
        member: Member,
        reason: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setTo(member.email)
                setSubject("❌ There was an issue with your order")
                setText(
                    """
    |Hi ${member.name},
    |
    |We're sorry, but we were unable to process your recent order.
    |
    |Please check your payment details and try again. If the problem persists, please contact our support team.
    |
    |Thanks,
    |Spaeti-SnackEnd
                    """.trimMargin(),
                )
            }

        mailSender.send(message)
    }

    override fun sendGiftNotification(
        buyer: Member,
        recipientEmail: String,
        order: Order,
        message: String?,
    ) {
        val itemsListString =
            order.items.joinToString("\n") { item ->
                val productName = item.productOption.product.name
                val optionName = item.productOption.name
                val quantity = item.quantity
                "- $quantity x $productName ($optionName)"
            }

        val mail =
            SimpleMailMessage().apply {
                setTo(recipientEmail)
                setSubject("🎁 You’ve received a gift from ${buyer.name}!")
                setText(
                    """
            |Hi there,
            |
            |${buyer.name} (${buyer.email}) sent you a gift!
            |
            |Order Date: ${order.orderDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
            |
            |Items:
            |$itemsListString
            |
            |${if (!message.isNullOrBlank()) "Message: $message\n\n" else ""}
            |Enjoy! 🍫🥤
            |
            |— Spaeti-SnackEnd
                    """.trimMargin(),
                )
            }
        mailSender.send(mail)
    }
}
