package ecommerce.integration

import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.repositories.ProductRepository
import ecommerce.services.ProductServiceJDBC
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProductServiceJDBCTest(
    @Autowired val productService: ProductServiceJDBC,
    @Autowired val productRepository: ProductRepository,
) {
    private lateinit var product: ProductDTO

    @BeforeEach
    fun setup() {
        product =
            ProductDTO(
                id = null,
                name = "Test Product",
                price = 19.99,
                imageUrl = "https://example.com/test.png",
            )
    }

    @Test
    fun `should save product`() {
        val saved = productService.save(product)

        assertThat(saved.id).isNotNull()
        assertThat(saved.name).isEqualTo(product.name)
        assertThat(saved.price).isEqualTo(product.price)
        assertThat(saved.imageUrl).isEqualTo(product.imageUrl)
    }

    @Test
    fun `should retrieve product by id`() {
        val saved = productService.save(product)
        val found = productService.findById(saved.id!!)

        assertThat(found.name).isEqualTo(saved.name)
        assertThat(found.price).isEqualTo(saved.price)
    }

    @Test
    fun `should update product by id`() {
        val saved = productService.save(product)
        val updated = saved.copy(name = "Updated", price = 49.99)

        val result = productService.updateById(saved.id!!, updated)

        assertThat(result.name).isEqualTo("Updated")
        assertThat(result.price).isEqualTo(49.99)
    }

    @Test
    fun `should patch product`() {
        val saved = productService.save(product)
        val patch = ProductPatchDTO(name = "Patched Name")

        val result = productService.patchById(saved.id!!, patch)

        assertThat(result.name).isEqualTo("Patched Name")
        assertThat(result.price).isEqualTo(saved.price)
    }

    @Test
    fun `should throw when saving product with duplicate name`() {
        productService.save(product)

        val ex =
            assertThrows<RuntimeException> {
                productService.save(product.copy(imageUrl = "https://different.com"))
            }

        assertThat(ex.message).contains("already exists")
    }

    @Test
    fun `should return all products`() {
        productService.save(product)
        productService.save(product.copy(name = "Second Product"))

        val all = productService.findAll()

        assertThat(all).hasSize(27)
    }

    @Test
    fun `should return paginated products`() {
        repeat(10) {
            productService.save(product.copy(name = "Product $it"))
        }

        val (items, total) = productService.findAllPaginated(page = 1, size = 4)

        assertThat(items).hasSize(4)
        assertThat(total).isEqualTo(35)
    }

    @Test
    fun `should delete product by id`() {
        val saved = productService.save(product)

        productService.deleteById(saved.id!!)

        assertThrows<DataAccessException> { productRepository.findById(saved.id!!) }
    }

    @Test
    fun `should delete all products`() {
        productService.save(product)
        productService.save(product.copy(name = "Another"))

        productService.deleteAll()

        assertThat(productService.findAll()).isEmpty()
    }

    @Test
    fun `should throw when retrieving nonexistent product`() {
        val ex =
            assertThrows<RuntimeException> {
                productService.findById(999L)
            }

        assertThat(ex.message).contains("Incorrect result")
    }
}
