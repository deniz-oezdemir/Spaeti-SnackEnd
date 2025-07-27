package ecommerce.endtoend

import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminE2ETest {
    lateinit var token: String

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
    }

    @Test
    @DisplayName("GET /admin/top-products returns 200 and product list")
    fun getTopProducts() {
        val products =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .get("/admin/top-products")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .jsonPath()
                .getList("", TopProductDTO::class.java)

        assertThat(products).isNotNull()
        assertThat(products).allSatisfy {
            assertThat(it.name).isNotBlank()
            assertThat(it.count).isGreaterThan(0)
            assertThat(it.mostRecentAddedAt).isBefore(LocalDateTime.now().plusMinutes(1))
        }
    }

    @Test
    @DisplayName("GET /admin/active-members returns 200 and member list")
    fun getActiveMembers() {
        val members =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .get("/admin/active-members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .jsonPath()
                .getList("", ActiveMemberDTO::class.java)

        assertThat(members).isNotEmpty
        assertThat(members).allSatisfy {
            assertThat(it.id).isPositive()
            assertThat(it.email).contains("@")
        }
    }

    @Test
    @DisplayName("GET /admin/top-products fails for non-admin token")
    fun getTopProductsUnauthorized() {
        val nonAdminToken = loginAsUser()

        RestAssured.given()
            .header("Authorization", "Bearer $nonAdminToken")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .get("/admin/top-products")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    private fun loginAsUser(): String {
        val loginPayload =
            mapOf(
                "email" to "user1@example.com",
                "password" to "pass",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/members/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()

        return response.body().jsonPath().getString("accessToken")
    }
}
