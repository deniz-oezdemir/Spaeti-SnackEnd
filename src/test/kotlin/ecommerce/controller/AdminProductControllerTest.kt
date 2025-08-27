package ecommerce.controller

import ecommerce.dto.MemberResponse
import ecommerce.dto.OptionCreateDto
import ecommerce.dto.ProductRequest
import ecommerce.enums.UserRole
import ecommerce.infrastructure.JWTProvider
import ecommerce.service.AuthService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminProductControllerTest {
    @LocalServerPort
    lateinit var port: Integer

    @MockitoBean
    private lateinit var jwtProvider: JWTProvider

    @MockitoBean
    private lateinit var authService: AuthService

    private val token = "mocked-jwt-token"

    @BeforeEach
    fun setup() {
        RestAssured.port = port.toInt()
        // let any token be “valid”
        doNothing().`when`(jwtProvider).validateToken(token)
        // return an ADMIN for this token
        `when`(authService.findMemberByToken(token)).thenReturn(
            MemberResponse(
                id = 1L,
                email = "admin@example.com",
                name = "Admin",
                role = UserRole.ADMIN.name,
                slackUserId = null,
            ),
        )
        // Make all RestAssured calls automatically include the Authorization header
        RestAssured.requestSpecification =
            RestAssured
                .given()
                .header("Authorization", "Bearer $token")
    }

    @Test
    fun `create a product`() {
        val productRequest =
            ProductRequest(
                name = "Product 1",
                price = 10.0,
                imageUrl = "http://localhost:8080/image/upload/product1.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
    }

    @Test
    fun `returns a product after creation`() {
        val productRequest =
            ProductRequest(
                name = "Mini Laptop",
                price = 299.99,
                imageUrl = "http://localhost:$port/image/upload/tablet.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )
        RestAssured.given()
            .log().all()
            .contentType(ContentType.JSON)
            .body(productRequest)
            .post("/api/protected/admin/products/create")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
    }

    @Test
    fun `update a product`() {
        val createRequest =
            ProductRequest(
                name = "Product",
                price = 10.0,
                imageUrl = "http://localhost:$port/image/upload/product1.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        // Create product first
        val createResponse =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value())

        // Then update it
        val productId =
            createResponse.header("Location")
                ?.substringAfterLast('/')
                ?.toLong()
                ?: error("Create response missing Location header")
        val updatedProduct =
            ProductRequest(
                name = "Updated Product",
                price = 20.0,
                imageUrl = "http://localhost:$port/image/upload/product2.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatedProduct)
                .put("/api/protected/admin/products/update/$productId")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun `delete a product`() {
        val productRequest =
            ProductRequest(
                name = "Product 10",
                price = 10.0,
                imageUrl = "http://localhost:$port/image/upload/product1.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        // Create the product
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(productRequest)
            .post("/api/protected/admin/products/create")
            .then()
            .statusCode(HttpStatus.CREATED.value())

        // Delete it
        val response =
            RestAssured.given()
                .delete("/api/protected/admin/products/delete/1")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())
    }

    // validation for create products
    @Test
    fun `create fails when product name is blank`() {
        val invalidProduct =
            ProductRequest(
                name = "",
                price = 10.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        println("Response body:\n${response.body()}")
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(response.asString()).contains("Product name must not be blank")
    }

    @Test
    fun `create fails when product name is too long`() {
        val invalidProduct =
            ProductRequest(
                name = "This name is definitely way too long",
                price = 10.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Product name must be between 1 and 15 characters")
    }

    @Test
    fun `create fails when product name has invalid special chars`() {
        val invalidProduct =
            ProductRequest(
                name = "Invalid@Name!",
                price = 10.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Invalid characters in product name", "name")
    }

    @Test
    fun `create fails when product price is zero`() {
        val invalidProduct =
            ProductRequest(
                name = "ValidName",
                price = 0.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Price must be positive", "price")
    }

    @Test
    fun `create fails when product price is negative`() {
        val invalidProduct =
            ProductRequest(
                name = "ValidName",
                price = -5.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Price must be positive", "price")
    }

    @Test
    fun `create fails when imageUrl does not start with http or https`() {
        val invalidProduct =
            ProductRequest(
                name = "ValidName",
                price = 10.0,
                imageUrl = "ftp://invalid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/api/protected/admin/products/create")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Image URL must start with http:// or https://", "imageUrl")
    }

// validation for update products

    @Test
    fun `update fails when product name is blank`() {
        val productId = 1L
        val invalidProduct =
            ProductRequest(
                name = "",
                price = 10.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .put("/api/protected/admin/products/update/$productId")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Product name must not be blank", "name")
    }

    @Test
    fun `update fails when product price is zero`() {
        val productId = 1L
        val invalidProduct =
            ProductRequest(
                name = "ValidName",
                price = 0.0,
                imageUrl = "http://valid-url.com/image.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .put("/api/protected/admin/products/update/$productId")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Price must be positive", "price")
    }

    @Test
    fun `update fails when imageUrl invalid`() {
        val productId = 1L
        val invalidProduct =
            ProductRequest(
                name = "ValidName",
                price = 10.0,
                imageUrl = "invalid-url",
                options =
                    mutableListOf(
                        OptionCreateDto(name = "Silver", quantity = 99),
                        OptionCreateDto(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .put("/api/protected/admin/products/update/$productId")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Image URL must start with http:// or https://", "imageUrl")
    }

    @Test
    fun `create product without token returns 401`() {
        val prev = RestAssured.requestSpecification
        try {
            RestAssured.requestSpecification = null

            val productRequest =
                ProductRequest(
                    name = "NoAuth",
                    price = 10.0,
                    imageUrl = "http://localhost:$port/image/noauth.jpg",
                    options = mutableListOf(OptionCreateDto("Silver", 1)),
                )

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/api/protected/admin/products/create")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
        } finally {
            RestAssured.requestSpecification = prev
        }
    }

    @Test
    fun `create product with non-admin role returns 403`() {
        val nonAdmin =
            MemberResponse(
                id = 2L,
                email = "user@example.com",
                name = "User",
                role = UserRole.USER.name,
                slackUserId = null,
            )

        // Make JWT validation succeed whether the interceptor sends raw or "Bearer ..." token
        doNothing().`when`(jwtProvider).validateToken(token)
        doNothing().`when`(jwtProvider).validateToken("Bearer $token")

        // Make AuthService return NON-ADMIN for either form
        `when`(authService.findMemberByToken(token)).thenReturn(nonAdmin)
        `when`(authService.findMemberByToken("Bearer $token")).thenReturn(nonAdmin)

        val productRequest =
            ProductRequest(
                name = "NotAllowed",
                price = 10.0,
                imageUrl = "http://localhost:$port/image/user.jpg",
                options = mutableListOf(OptionCreateDto("Black", 1)),
            )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $token")
            .body(productRequest)
            .post("/api/protected/admin/products/create")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }
}
