package ecommerce.infrastructure

import ecommerce.config.StripeProperties
import ecommerce.dto.PaymentRequest
import ecommerce.dto.StripeIntentResponse
import ecommerce.enums.PaymentMethod
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.net.URI

class StripeClientTest {
    private lateinit var server: MockRestServiceServer
    private lateinit var client: StripeClient
    private lateinit var builder: RestClient.Builder

    @BeforeEach
    fun setUp() {
        builder = RestClient.builder()
        server = MockRestServiceServer.bindTo(builder).build()

        client =
            StripeClient(
                stripeProperties = StripeProperties(secret = "TEST_SECRET_TOKEN"),
                builder = builder,
            )
    }

    @AfterEach
    fun tearDown() {
        server.verify()
    }

    @Test
    fun `createAndConfirmPayment returns parsed response on 200`() {
        val successJson =
            """
            {
              "id": "pi_123",
              "status": "succeeded",
              "client_secret": "pi_123_secret_abc"
            }
            """.trimIndent()

        server.expect(ExpectedCount.once(), requestTo(URI("https://api.stripe.com/v1/payment_intents")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer TEST_SECRET_TOKEN"))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .andExpect(content().string(containsString("amount=999")))
            .andExpect(content().string(containsString("currency=eur")))
            .andRespond(withSuccess(successJson, MediaType.APPLICATION_JSON))

        val req =
            PaymentRequest(
                amount = 999,
                currency = "eur",
                paymentMethod = PaymentMethod.PM_CARD_VISA.id,
            )

        val resp: StripeIntentResponse = client.createAndConfirmPayment(req)

        assertThat(resp.id).isEqualTo("pi_123")
        assertThat(resp.status).isEqualTo("succeeded")
        assertThat(resp.clientSecret).isEqualTo("pi_123_secret_abc")
    }

    @Test
    fun `createAndConfirmPayment throws IllegalArgumentException on Stripe error`() {
        val errorJson =
            """
            {
              "error": {
                "type": "card_error",
                "code": "card_declined",
                "message": "Your card was declined."
              }
            }
            """.trimIndent()

        server.expect(ExpectedCount.once(), requestTo(URI("https://api.stripe.com/v1/payment_intents")))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.PAYMENT_REQUIRED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson),
            )

        val req =
            PaymentRequest(
                amount = 5000,
                currency = "eur",
                paymentMethod = PaymentMethod.PM_CARD_CHARGE_CUSTOMER_FAIL.id,
            )

        val ex =
            assertThrows<IllegalArgumentException> {
                client.createAndConfirmPayment(req)
            }

        assertThat(ex.message).contains("Stripe error 402")
        assertThat(ex.message).contains("card_declined")
    }
}
