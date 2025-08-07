package ecommerce.repository

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
internal class ProductRepositoryJpaTest
    @Autowired
    constructor(
        private val repo: ProductRepositoryJpa,
    ) {
        @Test
        fun `save persists and assigns id`() {
            val saved = repo.save(sampleProduct())
            assertThat(saved.id).isNotNull()
        }

        @Test
        fun `count returns correct number of products`() {
            repo.save(sampleProduct(name = "One"))
            repo.save(sampleProduct(name = "Two"))
            assertThat(repo.count()).isEqualTo(2)
        }

        @Test
        fun `findById returns product when id exists`() {
            val saved = repo.save(sampleProduct())
            val found = repo.findById(saved.id!!)
            assertThat(found).isPresent
            assertThat(found.get().name).isEqualTo("Test Product")
        }

        @Test
        fun `findById returns empty when id does not exist`() {
            assertThat(repo.findById(999L)).isEmpty
        }

        @Test
        fun `findByName returns product when name exists`() {
            repo.save(sampleProduct(name = "Alpha"))
            val found = repo.findByName("Alpha")
            assertThat(found).isNotNull
            assertThat(found!!.name).isEqualTo("Alpha")
        }

        @Test
        fun `findByName returns null when name does not exist`() {
            assertThat(repo.findByName("Nonexistent")).isNull()
        }

        @Test
        fun `existsByName returns true when name exists`() {
            repo.save(sampleProduct(name = "Bravo"))
            assertThat(repo.existsByName("Bravo")).isTrue()
        }

        @Test
        fun `existsByName returns false when name does not exist`() {
            assertThat(repo.existsByName("Charlie")).isFalse()
        }

        @Test
        fun `should update existing product`() {
            val saved = repo.save(sampleProduct())
            saved.name = "Updated Product"
            assertThat(saved.name).isEqualTo("Updated Product")
        }

        @Test
        fun `delete removes the product`() {
            val saved = repo.save(sampleProduct())
            repo.delete(saved)
            assertThat(repo.existsById(saved.id!!)).isFalse()
        }

        private fun sampleProduct(
            name: String = "Test Product",
            price: Double = 9.99,
            imageUrl: String = "https://example.com/img.jpg",
            options: MutableList<Option> =
                mutableListOf(
                    Option(name = "Blue XL", quantity = 99),
                    Option(name = "Red Large", quantity = 42),
                ),
        ) = Product(
            name = name,
            price = price,
            imageUrl = imageUrl,
            options = options,
        )
    }
