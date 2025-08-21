package ecommerce.config

import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackConfig {
    @Value("\${slack.bot.token}")
    private lateinit var botToken: String

    @Bean
    fun slackInstance(): Slack {
        return Slack.getInstance()
    }

    @Bean
    fun slackMethods(slack: Slack): MethodsClient {
        return slack.methods(botToken)
    }
}
