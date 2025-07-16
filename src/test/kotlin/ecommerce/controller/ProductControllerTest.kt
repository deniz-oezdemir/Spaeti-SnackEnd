package ecommerce.controller

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductControllerTest {
    @Test
    fun getProducts() {
//        val response = RestAssured.given()
    }

    @Test
    fun createProduct() {
    }

    @Test
    fun updateProduct() {
    }

    @Test
    fun patchProduct() {
    }

    @Test
    fun deleteProduct() {
    }
}
