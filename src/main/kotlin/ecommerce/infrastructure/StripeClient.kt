package ecommerce.infrastructure

import ecommerce.config.StripeProperties
import ecommerce.dto.PaymentRequest
import ecommerce.dto.StripeIntentResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
    builder: RestClient.Builder,
) {
    private val restClient: RestClient = builder.build()

    fun createAndConfirmPayment(req: PaymentRequest): StripeIntentResponse {
        val body =
            listOf(
                "amount=${req.amount}",
                "currency=${req.currency}",
                "payment_method=${req.paymentMethod}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            restClient.post()
                .uri("https://api.stripe.com/v1/payment_intents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toEntity(StripeIntentResponse::class.java)
                .body ?: throw IllegalArgumentException("Stripe error: empty body")
        } catch (e: RestClientResponseException) {
            val reason = e.responseBodyAsString.take(500)
            throw IllegalArgumentException("Stripe error ${e.rawStatusCode}: $reason")
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}")
        }
    }
}
