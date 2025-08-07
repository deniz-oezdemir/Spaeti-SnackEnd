package ecommerce.integration

import ecommerce.exception.NotFoundException
import ecommerce.model.ProductPatchDTO
import ecommerce.model.ProductRequestDTO
import ecommerce.repositories.ProductRepository
import ecommerce.services.ProductService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class ProductServiceTest(
    @Autowired val productService: ProductService,
    @Autowired val productRepository: ProductRepository,
) {
    private lateinit var product: ProductRequestDTO

    @BeforeEach
    fun setup() {
        product =
            ProductRequestDTO(
                id = null,
                name = "Test Product",
                price = 19.99,
                imageUrl = "https://example.com/test.png",
                options = emptySet(),
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

        val request =
            ProductRequestDTO(
                updated.id,
                updated.name,
                price = updated.price,
                imageUrl = updated.imageUrl,
                options = updated.options.toSet(),
            )
        val result = productService.updateById(saved.id!!, request)!!

        assertThat(result.name).isEqualTo("Updated")
        assertThat(result.price).isEqualTo(49.99)
    }

    @Test
    fun `should patch product`() {
        val saved = productService.save(product)
        val patch = ProductPatchDTO(name = "Patched Name")

        val result = productService.patchById(saved.id!!, patch)!!

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
        val sortedByName: Pageable =
            PageRequest.of(0, 999, Sort.by("name"))
        val all = productService.findAll(sortedByName)

        assertThat(all).hasSize(27)
    }

    @Test
    fun `should delete product by id`() {
        val saved = productService.save(product)

        productService.deleteById(saved.id!!)

        assertThrows<NotFoundException> { productService.findById(saved.id!!) }
    }

    @Test
    fun `should delete all products`() {
        productService.save(product)
        productService.save(product.copy(name = "Another"))

        productService.deleteAll()

        val sortedByName: Pageable =
            PageRequest.of(0, 999, Sort.by("name"))
        assertThat(productService.findAll(sortedByName)).isEmpty()
    }

    @Test
    fun `should throw when retrieving nonexistent product`() {
        val ex =
            assertThrows<RuntimeException> {
                productService.findById(999L)
            }

        assertThat(ex.message).contains("Product with id=")
    }
}
