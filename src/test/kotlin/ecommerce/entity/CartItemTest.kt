package ecommerce.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class CartItemTest {
    @Test
    fun `creates cart item with valid quantity and sets createdAt`() {
        // given
        val cart = mock(Cart::class.java)
        val option = mock(Option::class.java)

        val before = LocalDateTime.now()

        // when
        val item =
            CartItem(
                cart = cart,
                productOption = option,
                quantity = 3L,
            )

        val after = LocalDateTime.now()

        // then
        assertThat(item.id).isNull()
        assertThat(item.cart).isSameAs(cart)
        assertThat(item.productOption).isSameAs(option)
        assertThat(item.quantity).isEqualTo(3L)
        assertThat(item.createdAt).isNotNull
        assertThat(item.createdAt).isAfterOrEqualTo(before)
        assertThat(item.createdAt).isBeforeOrEqualTo(after)
    }

    @Test
    fun `throws when quantity is zero`() {
        val cart = mock(Cart::class.java)
        val option = mock(Option::class.java)

        assertThatThrownBy {
            CartItem(cart = cart, productOption = option, quantity = 0L)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Quantity must be positive")
    }

    @Test
    fun `throws when quantity is negative`() {
        val cart = mock(Cart::class.java)
        val option = mock(Option::class.java)

        assertThatThrownBy {
            CartItem(cart = cart, productOption = option, quantity = -5L)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Quantity must be positive")
    }
}
