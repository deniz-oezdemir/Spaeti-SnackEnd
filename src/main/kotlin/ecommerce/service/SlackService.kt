package ecommerce.service

import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.MarkdownTextObject
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Payment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackService {
    private val slackMethods: MethodsClient
    private val logger = LoggerFactory.getLogger(SlackService::class.java)

    @Autowired
    constructor(
        @Value("\${slack.bot.token}") token: String,
    ) {
        this.slackMethods = Slack.getInstance().methods(token)
    }

    fun sendMessage(
        slackUserId: String,
        text: String,
    ) {
        val request =
            ChatPostMessageRequest.builder()
                .channel(slackUserId) // No .toString() needed
                .text(text)
                .build()
        slackMethods.chatPostMessage(request)
    }

    private fun sendImageMessage(
        slackUserId: String,
        text: String,
        imageUrl: String,
    ) {
        val message =
            ChatPostMessageRequest.builder()
                .channel(slackUserId)
                .text(text)
                .blocks(
                    listOf(
                        Blocks.section { section ->
                            section.text(MarkdownTextObject.builder().text(text).build())
                        },
                        Blocks.image { image ->
                            image.imageUrl(imageUrl).altText("Product image")
                        },
                    ),
                )
                .build()
        slackMethods.chatPostMessage(message)
    }

    fun slackCheckoutMessage(
        member: Member,
        option: Option,
        payment: Payment,
    ) {
        println(member.slackUserId)
        member.slackUserId?.let { slackUserId -> // Only proceed if not null
            val optionImage = option.product.imageUrl

            if (payment.status == "PAID") {
                println("paid")
                sendPaidNotification(slackUserId, optionImage)
            } else {
                sendPaymentFailedNotification(slackUserId)
            }
        } ?: run {
            logger.warn("Member ${member.id} has no Slack ID configured")
            // Optional: Fallback to email notification here
        }
    }

    private fun sendPaidNotification(
        slackUserId: String,
        imageUrl: String,
    ) {
        sendMessage(slackUserId, "Thank you for your purchase!")
        sendImageMessage(slackUserId, "Product image:", imageUrl)
    }

    private fun sendPaymentFailedNotification(slackUserId: String) {
        sendMessage(slackUserId, "Payment failed. Please try again.")
    }
}
