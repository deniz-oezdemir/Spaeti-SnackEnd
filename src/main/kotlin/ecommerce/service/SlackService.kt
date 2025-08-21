package ecommerce.service

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import ecommerce.entity.Member
import ecommerce.entity.Order
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SlackService(
    private val slackMethods: MethodsClient,
) {
    private val logger = LoggerFactory.getLogger(SlackService::class.java)

    fun sendOrderConfirmationSlack(
        member: Member,
        order: Order,
    ) {
        val slackUserId = member.slackUserId
        if (slackUserId.isNullOrBlank()) {
            logger.warn("Member ${member.id} has no Slack user ID, skipping DM notification")
            return
        }

        val blocks = buildOrderConfirmationBlocks(member, order)
        sendDirectMessage(slackUserId, blocks, "Your order #${order.id} is confirmed!")
    }

    fun sendOrderFailureSlack(
        member: Member,
        reason: String,
    ) {
        val slackUserId = member.slackUserId
        if (slackUserId.isNullOrBlank()) {
            logger.warn("Member ${member.id} has no Slack user ID, skipping DM notification")
            return
        }

        val blocks = buildOrderFailureBlocks(member, reason)
        sendDirectMessage(slackUserId, blocks, "There was an issue with your order")
    }

    private fun buildOrderConfirmationBlocks(
        member: Member,
        order: Order,
    ): List<LayoutBlock> {
        val itemsListString =
            order.items.joinToString("\n") { item ->
                val productName = item.productOption.product.name
                val optionName = item.productOption.name
                val quantity = item.quantity
                "- $quantity x $productName ($optionName)"
            }

        return listOf(
            Blocks.section { section ->
                section.text(
                    MarkdownTextObject.builder()
                        .text("*✅ Hi ${member.name}, your order #${order.id} is confirmed!*")
                        .build(),
                )
            },
            Blocks.section { section ->
                section.text(
                    MarkdownTextObject.builder()
                        .text(
                            """
                            |Order Date: ${order.orderDateTime}
                            |Status: ${order.status}
                            |
                            |Items Purchased:
                            |$itemsListString
                            |
                            |Thanks,
                            |Spaeti-SnackEnd
                            """.trimMargin(),
                        )
                        .build(),
                )
            },
        )
    }

    private fun buildOrderFailureBlocks(
        member: Member,
        reason: String,
    ): List<LayoutBlock> =
        listOf(
            Blocks.section { section ->
                section.text(
                    MarkdownTextObject.builder()
                        .text("*❌ Hi ${member.name}, there was an issue with your order*")
                        .build(),
                )
            },
            Blocks.section { section ->
                section.text(
                    MarkdownTextObject.builder()
                        .text(
                            """
                            |Reason: $reason
                            |
                            |Please check your payment details and try again. If the problem persists, contact our support team.
                            |
                            |Thanks,
                            |Spaeti-SnackEnd
                            """.trimMargin(),
                        )
                        .build(),
                )
            },
        )

    private fun sendDirectMessage(
        slackUserId: String,
        blocks: List<LayoutBlock>,
        fallbackText: String,
    ) {
        try {
            val channelId = openDirectMessageChannel(slackUserId)
            if (channelId == null) {
                logger.warn("Failed to open DM for Slack user $slackUserId")
                return
            }
            val request =
                ChatPostMessageRequest.builder()
                    .channel(channelId)
                    .blocks(blocks)
                    .text(fallbackText)
                    .build()
            slackMethods.chatPostMessage(request)
        } catch (e: Exception) {
            logger.error("Error while sending Slack DM to user $slackUserId", e)
        }
    }

    private fun openDirectMessageChannel(slackUserId: String): String? =
        try {
            slackMethods.conversationsOpen { it.users(listOf(slackUserId)) }
                .channel?.id
        } catch (e: Exception) {
            logger.error("Error opening DM channel for Slack user $slackUserId", e)
            null
        }
}
