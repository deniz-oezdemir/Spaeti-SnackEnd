package ecommerce.endtoend

import ecommerce.config.DatabaseSeeder
import ecommerce.entities.Member
import ecommerce.entities.Option
import ecommerce.model.OrderResponseDTO
import ecommerce.model.PaymentRequestDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.OrderRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class OrderE2ETest {
    @Autowired
    private lateinit var databaseSeeder: DatabaseSeeder

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

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
        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/members/login")
                .then().extract()

        token = response.body().jsonPath().getString("accessToken")

        member = memberRepository.findByEmail("user1@example.com")!!

        optionToPurchase = optionRepository.findAll().find { it.product?.name == "Car" && it.name == "Red Color" }!!
    }

    @Test
    fun `should create order and return details on successful payment`() {
        val initialStock = optionToPurchase.quantity
        val request =
            PaymentRequestDTO(
                optionId = optionToPurchase.id!!,
                quantity = 1,
                paymentMethod = "pm_card_visa",
            )

        val orderResponse =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().`as`(OrderResponseDTO::class.java)

        // check response DTO
        assertThat(orderResponse.orderId).isNotNull()
        assertThat(orderResponse.totalAmount).isEqualTo(optionToPurchase.product!!.price)
        assertThat(orderResponse.items).hasSize(1)
        assertThat(orderResponse.items[0].productName).isEqualTo("Car")
        assertThat(orderResponse.stripePaymentId).startsWith("pi_")

        // verify database state
        val updatedOption = optionRepository.findById(optionToPurchase.id!!).get()
        assertThat(updatedOption.quantity).isEqualTo(initialStock - 1)

        val orderInDb = orderRepository.findAll().firstOrNull()
        assertThat(orderInDb).isNotNull
        assertThat(orderInDb!!.member.id).isEqualTo(member.id)
    }

    @Test
    fun `should not create an order when payment fails`() {
        val initialOrderCount = orderRepository.count()
        val request =
            PaymentRequestDTO(
                optionId = optionToPurchase.id!!,
                quantity = 1,
                paymentMethod = "pm_card_visa_chargeDeclined",
            )

        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/orders")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())

        // Verify no new order created in DB
        assertThat(orderRepository.count()).isEqualTo(initialOrderCount)
    }

    @Test
    fun `should return 409 conflict when stock is insufficient`() {
        val initialStock = optionToPurchase.quantity
        val requestedQuantity = (initialStock + 1).toLong()
        val initialOrderCount = orderRepository.count()

        val request =
            PaymentRequestDTO(
                optionId = optionToPurchase.id!!,
                quantity = requestedQuantity,
                paymentMethod = "pm_card_visa",
            )

        val errorResponse =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract()
                .body()
                .jsonPath()

        // verify error message in response
        assertThat(errorResponse.getString("message")).isEqualTo("Not enough stock")

        // verify stock has NOT changed in DB
        val updatedOption = optionRepository.findById(optionToPurchase.id!!).get()
        assertThat(updatedOption.quantity).isEqualTo(initialStock)

        // verify no new order was created
        assertThat(orderRepository.count()).isEqualTo(initialOrderCount)
    }
}
