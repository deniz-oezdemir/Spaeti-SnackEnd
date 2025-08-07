package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OptionTest {
    @Test
    fun `should create Option with valid data`() {
        val option = Option(name = "Size M", quantity = 100)
        assertEquals("Size M", option.name)
        assertEquals(100, option.quantity)
        assertNull(option.id)
    }

    @Test
    fun `should throw exception if name exceeds 50 characters`() {
        val longName = "A".repeat(51)
        val exception =
            assertThrows<IllegalArgumentException> {
                Option(name = longName, quantity = 10)
            }
        assertEquals("Name must not exceed 50 characters", exception.message)
    }

    @Test
    fun `should throw exception if name contains invalid characters`() {
        val invalidName = "Invalid@Name!"
        val exception =
            assertThrows<IllegalArgumentException> {
                Option(name = invalidName, quantity = 10)
            }
        assertEquals("Invalid characters in option name", exception.message)
    }

    @Test
    fun `should throw exception if quantity is less than 1`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Option(name = "Valid Name", quantity = 0)
            }
        assertEquals("Quantity must be between 1 and 99,999,999", exception.message)
    }

    @Test
    fun `should throw exception if quantity is more than 99_999_999`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Option(name = "Valid Name", quantity = 100_000_000)
            }
        assertEquals("Quantity must be between 1 and 99,999,999", exception.message)
    }

    @Test
    fun `should decrease quantity if stock is sufficient`() {
        val option = Option(name = "Color Blue", quantity = 20)
        option.decreaseQuantity(5)
        assertEquals(15, option.quantity)
    }

    @Test
    fun `should throw exception if decrease amount is zero or negative`() {
        val option = Option(name = "Material Cotton", quantity = 10)

        val exception =
            assertThrows<IllegalArgumentException> {
                option.decreaseQuantity(0)
            }
        assertEquals("Amount must be positive", exception.message)
    }

    @Test
    fun `should throw exception if not enough quantity to decrease`() {
        val option = Option(name = "Material Silk", quantity = 3)

        val exception =
            assertThrows<IllegalStateException> {
                option.decreaseQuantity(5)
            }
        assertEquals("Insufficient stock for option id=null", exception.message)
    }
}
