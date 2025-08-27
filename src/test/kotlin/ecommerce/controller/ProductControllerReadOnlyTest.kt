package ecommerce.controller

import ecommerce.dto.OptionCreateDto
import ecommerce.dto.ProductRequest
import ecommerce.service.ProductService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductControllerReadOnlyTest {
    @LocalServerPort
    lateinit var port: Integer

    @Autowired
    lateinit var productService: ProductService

    @BeforeEach
    fun setup() {
        RestAssured.port = port.toInt()

        // seed snack products
        fun create(
            name: String,
            price: Double,
        ) = productService.createProduct(
            ProductRequest(
                name = name,
                price = price,
                imageUrl = "http://localhost:$port/image/$name.jpg",
                options =
                    mutableListOf(
                        OptionCreateDto("Default", 10),
                    ),
            ),
        )
        create("Chips", 2.50)
        create("Chocolate", 1.20)
        create("Cookies", 3.00)
    }

    @Test
    fun `GET products (unpaged) returns list without auth`() {
        val res =
            RestAssured.given()
                .accept(ContentType.JSON)
                .get("/products")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()

        val body = res.asString()
        assertThat(body).contains("Chips", "Chocolate", "Cookies")
    }

    @Test
    fun `GET products paged returns page with default sort by name asc`() {
        val res =
            RestAssured.given()
                .accept(ContentType.JSON)
                .get("/products/paged?page=0&size=2&sort=name,asc")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()

        val body = res.asString()
        // metadata present
        assertThat(body).contains("\"size\":2")
        // Sorted by name asc → Chips, Chocolate (Cookies comes later)
        assertThat(body).contains("Chips")
        assertThat(body).contains("Chocolate")
        assertThat(body).doesNotContain("Cookies")
    }

    @Test
    fun `GET products by price returns only matches`() {
        val res =
            RestAssured.given()
                .accept(ContentType.JSON)
                .get("/products/price?price=2.50&page=0&size=10")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()

        val body = res.asString()
        assertThat(body).contains("Chips")
        assertThat(body).doesNotContain("Chocolate")
        assertThat(body).doesNotContain("Cookies")
    }
}
