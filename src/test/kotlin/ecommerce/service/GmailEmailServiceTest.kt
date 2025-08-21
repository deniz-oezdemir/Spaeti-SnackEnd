package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Product
import jakarta.mail.Multipart
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mail.javamail.JavaMailSender
import java.math.BigDecimal
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class GmailEmailServiceTest {

    @Mock
    private lateinit var mockMailSender: JavaMailSender

    @InjectMocks
    private lateinit var emailService: GmailEmailService

    private val messageCaptor = argumentCaptor<MimeMessage>()
    private lateinit var testMember: Member
    private lateinit var testOrder: Order

    @BeforeEach
    fun setup() {
        whenever(mockMailSender.createMimeMessage()).thenAnswer {
            MimeMessage(Session.getInstance(Properties()))
        }

        testMember = Member(id = 1, name = "Deniz", email = "deniz@example.com", password = "pw", role = "USER")
        val product = Product(id = 101, name = "Club-Mate", price = 1.50, imageUrl = "https://example.com/mate.jpg")
        val option = Option(id = 201, product = product, name = "Original", quantity = 10)
        product.options.add(option)
        testOrder = Order(id = 99, memberId = 1, totalAmount = BigDecimal("3.00"))
        val orderItem = OrderItem(
            id = 301,
            order = testOrder,
            productOption = option,
            quantity = 2,
            price = 1.50,
            productName = "Club-Mate",
            optionName = "Original",
            productImageUrl = "https://example.com/mate.jpg",
        )
        testOrder.items.add(orderItem)
    }

    @Test
    fun `sendOrderConfirmation sends a correctly formatted HTML email`() {
        emailService.sendOrderConfirmation(testMember, testOrder)
        verify(mockMailSender).send(messageCaptor.capture())
        val sentMessage = messageCaptor.firstValue

        assertEquals("✅ Your Order is Confirmed!", sentMessage.subject)

        val multipart = sentMessage.content as Multipart
        val bodyPart = multipart.getBodyPart(0)
        val htmlContent = bodyPart.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        assertTrue(htmlContent.contains("Hi Deniz"))
        assertTrue(htmlContent.contains("<img src=\"https://example.com/mate.jpg\""))
        assertTrue(htmlContent.contains("Total Amount: EUR 3.00"))
    }

    @Test
    fun `sendGiftNotification sends correct email to recipient`() {
        val recipientEmail = "friend@example.com"
        val giftMessage = "Enjoy the snacks! 🎉"
        testOrder.isGift = true
        testOrder.giftRecipientEmail = recipientEmail
        testOrder.giftMessage = giftMessage

        emailService.sendGiftNotification(testMember, recipientEmail, testOrder, giftMessage)

        verify(mockMailSender).send(messageCaptor.capture())
        val sentMessage = messageCaptor.firstValue

        assertEquals("You’ve received a gift from Deniz!", sentMessage.subject)

        val multipart = sentMessage.content as Multipart
        val bodyPart = multipart.getBodyPart(0)
        val htmlContent = bodyPart.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        assertTrue(htmlContent.contains("<b>Deniz</b> (deniz@example.com) has sent you a gift"))
        assertTrue(htmlContent.contains("<p>\"$giftMessage\"</p>"))
    }

    @Test
    fun `sendOrderFailureNotification sends a simple failure email`() {
        val reason = "Your card was declined."
        emailService.sendOrderFailureNotification(testMember, reason)
        verify(mockMailSender).send(messageCaptor.capture())
        val sentMessage = messageCaptor.firstValue

        assertEquals("There was an issue with your order", sentMessage.subject)

        val multipart = sentMessage.content as Multipart
        val bodyPart = multipart.getBodyPart(0)
        val htmlContent = bodyPart.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        assertTrue(htmlContent.contains("Hi Deniz"))
        assertTrue(htmlContent.contains("❌ Order Processing Issue"))
    }
}
