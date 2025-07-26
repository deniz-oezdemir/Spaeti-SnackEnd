package ecommerce.endtoend

import ecommerce.model.ProductDTO
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductE2ETest {
    lateinit var token: String

    @BeforeEach
    fun loginAndGetToken() {
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
        assertThat(token).isNotBlank
    }

    @Test
    fun getProducts() {
        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val names = response.body().jsonPath().getList<String>("name")
        assertThat(names).isNotEmpty()
        assertThat(names.size).isEqualTo(25)
    }

    @Test
    fun getProduct() {
        val productDTO =
            ProductDTO(
                name = "Speaker",
                price = 99.99,
                imageUrl = "http://iteha",
            )
        val id =
            RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(productDTO)
                .post("/api/products")
                .then().extract().jsonPath().getLong("id")

        val response =
            RestAssured.given()
                .get("/api/products/$id")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("name")).isEqualTo("Speaker")
        assertThat(response.body().jsonPath().getFloat("price")).isEqualTo(99.99f)
    }

    @Test
    fun getProduct_notFound() {
        val response =
            RestAssured.given()
                .auth().oauth2(token) // <-- add this line to send the token
                .get("/api/products/999999")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun createProduct() {
        val newProductDTO = ProductDTO(name = "Monitor", price = 150.0, imageUrl = "https://example.com/monitor.jpg")

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(newProductDTO)
                .`when`().post("/api/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        assertThat(response.body().jsonPath().getString("name")).isEqualTo("Monitor")
        assertThat(response.body().jsonPath().getFloat("price")).isEqualTo(150.0f)
    }

    @Test
    fun `Should return error when product name use invalid characters`() {
        val newProductDTO = ProductDTO(name = "**$$", price = 150.0, imageUrl = "https://example.com/monitor.jpg")

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(newProductDTO)
                .`when`().post("/api/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(response.body().jsonPath().getInt("status")).isEqualTo(400)
        assertThat(
            response.body().jsonPath().getString("message.name"),
        ).isEqualTo("Invalid characters in product name.")
    }

    @Test
    fun `Should return error when product name is bigger than 15 characters`() {
        val newProductDTO =
            ProductDTO(name = "123456789123456789", price = 150.0, imageUrl = "https://example.com/monitor.jpg")

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(newProductDTO)
                .`when`().post("/api/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(response.body().jsonPath().getInt("status")).isEqualTo(400)
        assertThat(
            response.body().jsonPath().getString("message.name"),
        ).isEqualTo("The product name must contain between 1 and 15 characters")
    }

    @Test
    fun `Should return error when product name already exists`() {
        val dto = ProductDTO(name = "Mouse", price = 30.0, imageUrl = "https://example.com/mouse.jpg")
        RestAssured.given().auth().oauth2(token)
            .contentType(ContentType.JSON).body(dto).post("/api/products")

        val duplicate =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token) // <-- add this line to send the token
                .body(dto)
                .post("/api/products")
                .then().extract()

        assertThat(duplicate.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(duplicate.body().jsonPath().getString("message")).contains("already exists")
    }

    @Test
    fun `Should return error when product price is negative value`() {
        val newProductDTO =
            ProductDTO(name = "Pizza", price = -150.0, imageUrl = "https://example.com/monitor.jpg")

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(newProductDTO)
                .`when`().post("/api/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(response.body().jsonPath().getInt("status")).isEqualTo(400)
        assertThat(
            response.body().jsonPath().getString("message.price"),
        ).contains("must be greater than zero")
    }

    @Test
    fun updateProduct() {
        val created = ProductDTO(name = "Mouse", price = 25.0, imageUrl = "https://example.com/mouse.jpg")
        val id =
            RestAssured.given()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(created)
                .post("/api/products")
                .then().extract().jsonPath().getLong("id")

        val updated = ProductDTO(name = "Gaming Mouse", price = 45.0, imageUrl = "https://example.com/gaming-mouse.jpg")

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(updated)
                .put("/api/products/$id")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("name")).isEqualTo("Gaming Mouse")
        assertThat(response.body().jsonPath().getFloat("price")).isEqualTo(45.0f)
    }

    @Test
    fun patchProduct() {
        val created = ProductDTO(name = "Tablet", price = 299.0, imageUrl = "https://example.com/tablet.jpg")
        val id =
            RestAssured.given()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(created)
                .post("/api/products")
                .then().extract().jsonPath().getLong("id")

        val patch = mapOf("price" to 249.0)

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(patch)
                .patch("/api/products/$id")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getFloat("price")).isEqualTo(249.0f)
    }

    @Test
    fun deleteProduct() {
        val created = ProductDTO(name = "Keyboard", price = 59.99, imageUrl = "https://example.com/keyboard.jpg")
        val id =
            RestAssured.given()
                .auth().oauth2(token) // <-- add this line to send the token
                .contentType(ContentType.JSON)
                .body(created)
                .post("/api/products")
                .then().extract().jsonPath().getLong("id")

        val deleteResponse =
            RestAssured.given()
                .auth().oauth2(token) // <-- add this line to send the token
                .delete("/api/products/$id")
                .then().log().all().extract()

        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())

        val getResponse =
            RestAssured.given()
                .auth().oauth2(token) // <-- add this line to send the token
                .get("/api/products/$id")
                .then().log().all().extract()

        assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}
