package ecommerce.endtoend

import ecommerce.config.DatabaseSeeder
import ecommerce.entities.CartItem
import ecommerce.entities.Member
import ecommerce.entities.Option
import ecommerce.model.PaymentRequestDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.OptionRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class PaymentE2ETest {

    @Autowired
    private lateinit var databaseSeeder: DatabaseSeeder
    @Autowired
    private lateinit var optionRepository: OptionRepository
    @Autowired
    private lateinit var cartItemRepository: CartItemRepository
    @Autowired
    private lateinit var memberRepository: MemberRepository

    private lateinit var token: String
    private lateinit var member: Member
    private lateinit var optionToPurchase: Option

    @AfterEach
    fun cleanup() {
        databaseSeeder.cleanup()
        databaseSeeder.seed()
    }

    @BeforeEach
    fun setup() {
        val loginPayload = mapOf("email" to "user1@example.com", "password" to "pass")
        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
            .post("/api/members/login")
            .then().extract()

        token = response.body().jsonPath().getString("accessToken")

        member = memberRepository.findByEmail("user1@example.com")!!

        optionToPurchase = optionRepository.findAll().find { it.name == "Red Color" }!!

        cartItemRepository.save(
            CartItem(
                member = member,
                product = optionToPurchase.product!!,
                quantity = 2,
                addedAt = LocalDateTime.now()
            )
        )
    }

    @Test
    fun `should complete payment, decrease stock, and remove cart item on success`() {
        val initialStock = optionToPurchase.quantity
        val request = PaymentRequestDTO(
            optionId = optionToPurchase.id!!,
            quantity = 1,
            paymentMethod = "pm_card_visa"
        )

        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/payments")
            .then()
            .statusCode(HttpStatus.OK.value())

        // stock has been decreased in the database
        val updatedOption = optionRepository.findById(optionToPurchase.id!!).get()
        assertThat(updatedOption.quantity).isEqualTo(initialStock - 1)

        // item has been removed from the cart
        val cartItemExists = cartItemRepository.findByProductIdAndMemberId(optionToPurchase.product!!.id!!, member.id!!)
        assertThat(cartItemExists).isNull()
    }

    @Test
    fun `should return error and not change database state when payment fails`() {
        // Arrange
        val initialStock = optionToPurchase.quantity
        val request = PaymentRequestDTO(
            optionId = optionToPurchase.id!!,
            quantity = 1,
            paymentMethod = "pm_card_visa_chargeDeclined" // Stripe's test ID for a failed payment
        )

        val errorResponse = RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/payments")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .body()
            .jsonPath()

        // error message from Stripe is in the response
        assertThat(errorResponse.getString("message")).contains("Your card was declined.")

        // stock has NOT changed
        val updatedOption = optionRepository.findById(optionToPurchase.id!!).get()
        assertThat(updatedOption.quantity).isEqualTo(initialStock)

        // item is still in the cart
        val cartItemExists = cartItemRepository.findByProductIdAndMemberId(optionToPurchase.product!!.id!!, member.id!!)
        assertThat(cartItemExists).isNotNull()
    }

    @Test
    fun `should return 409 conflict when stock is insufficient`() {
        val initialStock = optionToPurchase.quantity
        val requestedQuantity = initialStock + 1

        val request = PaymentRequestDTO(
            optionId = optionToPurchase.id!!,
            quantity = requestedQuantity,
            paymentMethod = "pm_card_visa"
        )

        val errorResponse = RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/payments")
            .then()
            .statusCode(HttpStatus.CONFLICT.value()) // We expect a 409 Conflict status
            .extract()
            .body()
            .jsonPath()

        // error message in the response
        assertThat(errorResponse.getString("message")).isEqualTo("Not enough stock")

        // stock has NOT changed in the database
        val updatedOption = optionRepository.findById(optionToPurchase.id!!).get()
        assertThat(updatedOption.quantity).isEqualTo(initialStock)
    }
}
