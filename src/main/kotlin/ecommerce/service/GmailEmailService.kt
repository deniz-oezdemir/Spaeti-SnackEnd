package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Order
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
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
        // use MimeMessage for HTML content
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        // The 'true' flag enables multipart mode, which is needed for HTML emails
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

        // build an HTML table for the order items
        val itemsHtml =
            order.items.joinToString("") { item ->
                val product = item.productOption.product
                val optionName = item.productOption.name
                val quantity = item.quantity
                """
                <tr style="border-bottom: 1px solid #ddd;">
                    <td style="padding: 10px;">
                        <img src="${product.imageUrl}" alt="${product.name}" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;">
                    </td>
                    <td style="padding: 10px; vertical-align: middle;">
                        ${product.name} ($optionName)
                        <br>
                        <span style="color: #555;">Quantity: $quantity</span>
                    </td>
                </tr>
                """.trimIndent()
            }

        // choose the correct subject and title based on whether it's a gift
        val subject: String
        val title: String
        val intro: String

        if (order.isGift) {
            subject = "🎁 Your gift purchase is confirmed!"
            title = "Gift Purchase Confirmed"
            intro = "Thank you for your purchase! We've successfully sent your gift to ${order.giftRecipientEmail}."
        } else {
            subject = "✅ Your Order is Confirmed!"
            title = "Order Confirmed"
            intro = "Thank you for your purchase! We've successfully received your order."
        }

        // construct the full HTML body
        val htmlBody =
            """
            <html lang="en">
            <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="color: #4CAF50;">$title</h2>
                    <p>Hi ${member.name},</p>
                    <p>$intro</p>
                    <hr>
                    <p><b>Order Date:</b> ${order.orderDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}</p>
                    <p><b>Status:</b> ${order.status}</p>
                    
                    <h3 style="margin-top: 25px;">Items Purchased:</h3>
                    <table style="width: 100%; border-collapse: collapse; text-align: left;">
                        <tbody>
                            $itemsHtml
                        </tbody>
                    </table>
                    
                    <h3 style="text-align: right; margin-top: 20px;">
                        Total Amount: EUR ${String.format("%.2f", order.totalAmount)}
                    </h3>
                    
                    <p style="margin-top: 30px;">Thanks,<br>Spaeti-SnackEnd</p>
                </div>
            </body>
            </html>
            """.trimIndent()

        // set the details on the helper and send
        helper.setTo(member.email)
        helper.setSubject(subject)
        helper.setText(htmlBody, true) // The 'true' flag indicates the text is HTML

        mailSender.send(mimeMessage)
    }

    override fun sendOrderFailureNotification(
        member: Member,
        reason: String,
    ) {
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

        // construct the HTML body for the failure notification
        val htmlBody =
            """
            <html lang="en">
            <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="color: #D32F2F;">❌ Order Processing Issue</h2>
                    <p>Hi ${member.name},</p>
                    <p>We're sorry, but we were unable to process your recent order.</p>
                    
                    <p>Please check your payment details and try again. If the problem persists, don't hesitate to contact our support team.</p>
                    
                    <p style="margin-top: 30px;">Thanks,<br>Spaeti-SnackEnd</p>
                </div>
            </body>
            </html>
            """.trimIndent()

        helper.setTo(member.email)
        helper.setSubject("There was an issue with your order")
        helper.setText(htmlBody, true)

        mailSender.send(mimeMessage)
    }

    override fun sendGiftNotification(
        buyer: Member,
        recipientEmail: String,
        order: Order,
        message: String?,
    ) {
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

        // build the same HTML item list as in the confirmation email
        val itemsHtml =
            order.items.joinToString("") { item ->
                val product = item.productOption.product
                val optionName = item.productOption.name
                val quantity = item.quantity
                """
                <tr style="border-bottom: 1px solid #ddd;">
                    <td style="padding: 10px;">
                        <img src="${product.imageUrl}" alt="${product.name}" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;">
                    </td>
                    <td style="padding: 10px; vertical-align: middle;">
                        ${product.name} ($optionName)
                        <br>
                        <span style="color: #555;">Quantity: $quantity</span>
                    </td>
                </tr>
                """.trimIndent()
            }

        // conditionally create the message block
        val messageHtml =
            if (!message.isNullOrBlank()) {
                """
                <h3 style="margin-top: 25px;">A message from ${buyer.name}:</h3>
                <div style="background-color: #f1f8e9; border-left: 4px solid #7cb342; padding: 10px 15px; margin: 10px 0; font-style: italic;">
                    <p>"$message"</p>
                </div>
                """.trimIndent()
            } else {
                ""
            }

        // construct the full HTML body for the gift notification
        val htmlBody =
            """
            <html lang="en">
            <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="color: #673AB7;">🎁 You've Received a Gift!</h2>
                    <p>Hi there,</p>
                    <p>You have a delicious surprise! <b>${buyer.name}</b> (${buyer.email}) has sent you a gift from Spaeti-SnackEnd.</p>
                    
                    <h3 style="margin-top: 25px;">Here's what you got:</h3>
                    <table style="width: 100%; border-collapse: collapse; text-align: left;">
                        <tbody>
                            $itemsHtml
                        </tbody>
                    </table>
                    
                    $messageHtml
                    
                    <p style="margin-top: 30px;">Enjoy,<br>Spaeti-SnackEnd</p>

                </div>
            </body>
            </html>
            """.trimIndent()

        helper.setTo(recipientEmail)
        helper.setSubject("You’ve received a gift from ${buyer.name}!")
        helper.setText(htmlBody, true)

        mailSender.send(mimeMessage)
    }
}
