package ecommerce.integration

import ecommerce.infrastructure.StripeClient
import ecommerce.model.StripePaymentRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun `should return a successful payment intent from Stripe`() {
        val request =
            StripePaymentRequest(
                amount = 1000,
                currency = "usd",
                paymentMethod = "pm_card_visa",
            )

        val responseJson = stripeClient.createPaymentIntent(request)

        assertThat(responseJson).isNotNull()
        assertThat(responseJson).contains("\"status\": \"succeeded\"")
    }

    @Test
    fun `should throw an exception for a declined card`() {
        val request =
            StripePaymentRequest(
                amount = 1000,
                currency = "usd",
                paymentMethod = "pm_card_visa_chargeDeclined",
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                stripeClient.createPaymentIntent(request)
            }

        assertThat(exception.message).contains("Your card was declined.")
    }
}
