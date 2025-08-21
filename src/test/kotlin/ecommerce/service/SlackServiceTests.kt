package ecommerce.service

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.ConversationsOpenRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.conversations.ConversationsOpenResponse
import com.slack.api.model.Conversation
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Product
import ecommerce.enums.OrderStatus
import ecommerce.repository.MemberRepositoryJpa
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime

@SpringBootTest
class SlackServiceTests {
    @Autowired
    private lateinit var memberRepositoryJpa: MemberRepositoryJpa

    @Autowired
    private lateinit var slackService: SlackService

    @MockitoBean
    private lateinit var slackMethods: MethodsClient

    private lateinit var order: Order

    @BeforeEach
    fun setupTestData() {
        memberRepositoryJpa.deleteAll()
        val member =
            memberRepositoryJpa.save(
                Member(
                    name = "Test User",
                    email = "test@example.com",
                    password = "testpassword",
                    role = "USER",
                    slackUserId = "U123456",
                ),
            )

        memberRepositoryJpa.save(
            Member(
                name = "No Slack User",
                email = "noslack@example.com",
                password = "testpassword",
                role = "USER",
                slackUserId = null,
            ),
        )

        memberRepositoryJpa.save(
            Member(
                name = "Empty Slack User",
                email = "emptyslack@example.com",
                password = "testpassword",
                role = "USER",
                slackUserId = "",
            ),
        )

        val product =
            Product(
                name = "Test Product",
                price = 1.25,
                imageUrl = "https://example.png",
                options = mutableListOf(),
            )
        val option =
            Option(
                name = "Default",
                quantity = 2,
                product = product,
            )
        product.options.add(option)

        order =
            Order(
                id = 1L,
                memberId = member.id!!,
                items = mutableListOf(),
                status = OrderStatus.PAID,
                orderDateTime = LocalDateTime.now(),
            )
        val orderItem =
            OrderItem(
                productOption = option,
                quantity = 2,
                order = order,
                price = 1.25,
                productName = "Test Product",
                optionName = "Default",
                productImageUrl = "https://example.png",
            )
        order.items.add(orderItem)
    }

    @Test
    fun `should send order confirmation slack message`() {
        val member = memberRepositoryJpa.findAll().first { it.slackUserId == "U123456" }
        val conversation = Mockito.mock(Conversation::class.java)
        Mockito.`when`(conversation.id).thenReturn("C123456")

        val conversationsOpenResponse = Mockito.mock(ConversationsOpenResponse::class.java)
        Mockito.`when`(conversationsOpenResponse.channel).thenReturn(conversation)

        Mockito.`when`(
            slackMethods.conversationsOpen(Mockito.any(ConversationsOpenRequest::class.java)),
        ).thenReturn(conversationsOpenResponse)

        Mockito.`when`(slackMethods.chatPostMessage(Mockito.any(ChatPostMessageRequest::class.java)))
            .thenReturn(Mockito.mock(ChatPostMessageResponse::class.java))

        assertDoesNotThrow {
            slackService.sendOrderConfirmationSlack(member, order)
        }
    }

    @Test
    fun `should not send message if slackUserId is null`() {
        val member = memberRepositoryJpa.findAll().first { it.slackUserId == null }
        assertDoesNotThrow {
            slackService.sendOrderConfirmationSlack(member, order)
        }
        Mockito.verifyNoInteractions(slackMethods)
    }

    @Test
    fun `should not send message if slackUserId is empty`() {
        val member = memberRepositoryJpa.findAll().first { it.slackUserId == "" }
        assertDoesNotThrow {
            slackService.sendOrderFailureSlack(member)
        }
        Mockito.verifyNoInteractions(slackMethods)
    }

    @Test
    fun `should handle exception from Slack API`() {
        val member = memberRepositoryJpa.findAll().first { it.slackUserId == "U123456" }

        Mockito.`when`(
            slackMethods.conversationsOpen(Mockito.any(ConversationsOpenRequest::class.java)),
        ).thenThrow(RuntimeException("Slack API error"))

        assertDoesNotThrow {
            slackService.sendOrderConfirmationSlack(member, order)
        }
    }
}
