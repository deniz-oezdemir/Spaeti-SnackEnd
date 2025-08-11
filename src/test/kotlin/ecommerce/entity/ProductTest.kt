package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProductTest {
    @Test
    fun `should create ProductEntity with valid data`() {
        val options = mutableListOf(sampleOption())

        val product =
            Product(
                name = "T-Shirt",
                price = 19.99,
                imageUrl = "https://example.com/image.png",
                options = options,
            )

        assertEquals("T-Shirt", product.name)
        assertEquals(19.99, product.price)
        assertEquals("https://example.com/image.png", product.imageUrl)
        assertEquals(1, product.options.size)
    }

    @Test
    fun `should throw error when name is blank`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product(
                    name = " ",
                    price = 19.99,
                    imageUrl = "https://example.com/image.png",
                    options = mutableListOf(sampleOption()),
                )
            }
        assertEquals("Product name must not be blank", exception.message)
    }

    @Test
    fun `should throw error when price is zero or negative`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product(
                    name = "T-Shirt",
                    price = 0.0,
                    imageUrl = "https://example.com/image.png",
                    options = mutableListOf(sampleOption()),
                )
            }
        assertEquals("Price must be positive", exception.message)
    }

    @Test
    fun `should throw error when imageUrl does not start with http`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product(
                    name = "T-Shirt",
                    price = 19.99,
                    imageUrl = "ftp://example.com/image.png",
                    options = mutableListOf(sampleOption()),
                )
            }
        assertEquals("Image URL must start with http:// or https://", exception.message)
    }

    @Test
    fun `should consider two ProductEntity objects equal if IDs are equal`() {
        val id = 1L
        val options = mutableListOf(sampleOption())

        val product1 =
            Product(id = id, name = "Product A", price = 10.0, imageUrl = "https://img.com", options = options)
        val product2 =
            Product(id = id, name = "Product B", price = 20.0, imageUrl = "https://img.com", options = options)

        assertEquals(product1, product2)
        assertEquals(product1.hashCode(), product2.hashCode())
    }

    @Test
    fun `should not consider two ProductEntity objects equal if IDs are different`() {
        val options = mutableListOf(sampleOption())

        val product1 =
            Product(id = 1L, name = "Product A", price = 10.0, imageUrl = "https://img.com", options = options)
        val product2 =
            Product(id = 2L, name = "Product B", price = 20.0, imageUrl = "https://img.com", options = options)

        assertNotEquals(product1, product2)
    }

    private fun createSampleProduct(): Product {
        val product =
            Product(
                name = "Product",
                price = 10.0,
                imageUrl = "https://image/image.jpg",
            )
        return product
    }

    private fun sampleOption() = Option(product = createSampleProduct(), name = "Size M", quantity = 2)
}
