package ecommerce.controller

import ecommerce.dto.ProductRequest
import ecommerce.entity.Option
import ecommerce.entity.Product
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductControllerTest {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )
        RestAssured.given()
            .log().all()
            .contentType(ContentType.JSON)
            .body(productRequest)
            .post("/products")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())

        val response =
            RestAssured.given()
                .log().all()
                .get("/products")
                .then()
                .log().all()
                .extract()
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val products: List<Product> =
            response.body().jsonPath().getList("", Product::class.java)
        assertThat(products).anyMatch { it.name == "Mini Laptop" }
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        // Create product first
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value())

        // Then update it
        val productId = 1L
        val updatedProduct =
            ProductRequest(
                name = "Updated Product",
                price = 20.0,
                imageUrl = "http://localhost:$port/image/upload/product2.jpg",
                options =
                    mutableListOf(
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatedProduct)
                .put("/products/$productId")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        // Create the product
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(productRequest)
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value())

        // Delete it
        val response =
            RestAssured.given()
                .delete("/products/1")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .post("/products")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .put("/products/$productId")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .put("/products/$productId")
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
                        Option(name = "Silver", quantity = 99),
                        Option(name = "Black", quantity = 42),
                    ),
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .put("/products/$productId")
                .then()
                .extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        Assertions.assertThat(response.asString()).contains("Image URL must start with http:// or https://", "imageUrl")
    }
}
