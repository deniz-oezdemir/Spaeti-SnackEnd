package ecommerce.integration

import ecommerce.entities.Product
import ecommerce.model.CartItemRequestDTO
import ecommerce.repositories.ProductRepository
import ecommerce.services.CartItemServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class CartItemServiceTest {
    @Autowired
    private lateinit var cartItemService: CartItemServiceImpl

    @Autowired
    private lateinit var productRepository: ProductRepository

    private var productId: Long = 0
    private val memberId = 1L

    @BeforeEach
    fun setup() {
        val product =
            Product(
                id = null,
                name = "Keyboard",
                price = 50.0,
                imageUrl = "keyboard.png",
            )

        productId = productRepository.save(product)?.id!!
    }

    @Test
    fun `addOrUpdate should create new cart item if not exists`() {
        val dto = CartItemRequestDTO(productId = productId, quantity = 2)

        val response = cartItemService.addOrUpdate(dto, memberId)

        assertThat(response.id).isNotNull()
        assertThat(response.quantity).isEqualTo(2)
        assertThat(response.product.name).isEqualTo("Keyboard")
    }

    @Test
    fun `addOrUpdate should update quantity if item exists`() {
        val initial = CartItemRequestDTO(productId = productId, quantity = 1)
        cartItemService.addOrUpdate(initial, memberId)

        val updated = CartItemRequestDTO(productId = productId, quantity = 5)
        val result = cartItemService.addOrUpdate(updated, memberId)

        assertThat(result.quantity).isEqualTo(5)
    }

    @Test
    fun `addOrUpdate should throw if product not found`() {
        val badDto = CartItemRequestDTO(productId = 9999L, quantity = 1)

        assertThrows<EmptyResultDataAccessException> {
            cartItemService.addOrUpdate(badDto, memberId)
        }
    }

    @Test
    fun `findByMember should return cart items for a member`() {
        cartItemService.addOrUpdate(CartItemRequestDTO(productId, 3), memberId)

        val items = cartItemService.findByMember(memberId)

        assertThat(items).hasSize(1)
        assertThat(items[0].product.id).isEqualTo(productId)
        assertThat(items[0].quantity).isEqualTo(3)
    }

    @Test
    fun `delete should remove cart item for member`() {
        cartItemService.addOrUpdate(CartItemRequestDTO(productId, 2), memberId)

        cartItemService.delete(CartItemRequestDTO(productId, 2), memberId)

        val items = cartItemService.findByMember(memberId)
        assertThat(items).isEmpty()
    }
}
