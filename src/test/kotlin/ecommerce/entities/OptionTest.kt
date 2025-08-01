package ecommerce.entities

import ecommerce.exception.InsufficientStockException
import ecommerce.exception.InvalidOptionQuantityException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {
    @Test
    fun `subtract should reduce quantity correctly`() {
        val option = Option(name = "Test", quantity = 10)
        option.subtract(3)
        assertEquals(7, option.quantity)
    }

    @Test
    fun `subtract should throw if quantity is less than 1`() {
        val option = Option(name = "Test", quantity = 10)
        assertThrows<InvalidOptionQuantityException> {
            option.subtract(0)
        }
    }

    @Test
    fun `subtract should throw if subtracting more than stock`() {
        val option = Option(name = "Test", quantity = 5)
        assertThrows<InsufficientStockException> {
            option.subtract(10)
        }
    }
}