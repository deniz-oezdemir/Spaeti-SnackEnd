package ecommerce.endtoend

import ecommerce.model.WishItemRequestDTO
import ecommerce.model.WishItemResponseDTO
import ecommerce.model.ProductDTO
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class WishItemE2ETest {

    lateinit var token: String
    private val productId: Long = 1L
    private val request get() = WishItemRequestDTO(productId = productId)

    @BeforeEach
    fun setup() {
        val loginPayload =
            mapOf(
                "email" to "sebas@sebas.com",
                "password" to "123456",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/members/login")
                .then().extract()

        token = response.body().jsonPath().getString("accessToken")
        assertThat(token).isNotBlank()

        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .get("/api/wish")
            .then().extract()
            .body()
            .jsonPath()
            .getList("", WishItemResponseDTO::class.java)
            .forEach {
                RestAssured.given()
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(WishItemRequestDTO((it as WishItemResponseDTO).product.id!!))
                    .delete("/api/wish")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value())
            }
    }

    @Test
    fun `add wish item`() {
        val wishItem = addWishItemAndReturn()

        assertThat(wishItem.product).isInstanceOf(ProductDTO::class.java)
        assertThat(wishItem.product.id).isEqualTo(productId)
        assertThat(wishItem.addedAt).isBefore(LocalDateTime.now().plusMinutes(1))
    }

    @Test
    fun `get wish items`() {
        addWishItemAndReturn()

        val items =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .get("/api/wish")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().body().jsonPath().getList("", WishItemResponseDTO::class.java)

        assertThat(items).hasSize(1)

        val wishItem = items.first()
        with(wishItem) {
            assertThat(product.id).isEqualTo(productId)
            assertThat(addedAt).isBefore(LocalDateTime.now().plusMinutes(1))
            assertThat(addedAt).isAfter(LocalDateTime.now().minusDays(1))
        }
    }

    @Test
    fun `delete wish item`() {
        val addedItem = addWishItemAndReturn()

        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .delete("/api/wish")
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value())

        val items =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .get("/api/wish")
                .then().extract().body().jsonPath().getList("", WishItemResponseDTO::class.java)

        assertThat(items).isEmpty()
    }

    private fun addWishItemAndReturn(): WishItemResponseDTO {
        return RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .post("/api/wish")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .`as`(WishItemResponseDTO::class.java)
    }
}