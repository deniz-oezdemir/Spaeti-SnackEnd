package ecommerce.integration

import ecommerce.entities.Product
import ecommerce.mappers.toDto
import ecommerce.model.CartItemRequestDTO
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.ProductRepository
import ecommerce.services.CartItemService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class CartItemServiceTest {
    @Autowired
    private lateinit var cartItemService: CartItemService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

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

        productId = productRepository.save(product).id!!
    }

    @Test
    fun `addOrUpdate should create new cart item if not exists`() {
        val dto = CartItemRequestDTO(productId = productId, quantity = 2)
        val member = memberRepository.findByIdOrNull(memberId)?.toDto()

        val response = cartItemService.addOrUpdate(dto, member!!)

        assertThat(response.id).isNotNull()
        assertThat(response.quantity).isEqualTo(2)
        assertThat(response.product.name).isEqualTo("Keyboard")
    }

    @Test
    fun `addOrUpdate should update quantity if item exists`() {
        val initial = CartItemRequestDTO(productId = productId, quantity = 1)
        val member = memberRepository.findByIdOrNull(memberId)?.toDto()
        val newItem = cartItemService.addOrUpdate(initial, member!!)

        val updated = CartItemRequestDTO(productId = productId, quantity = 5)
        val result = cartItemService.addOrUpdate(updated, member)

        assertThat(result.quantity).isEqualTo(5)
    }
//    @Test
//    fun `addOrUpdate should update quantity if item exists`() {
//        val initial = CartItemRequestDTO(productId = productId, quantity = 1)
//        val member = memberRepository.findByIdOrNull(memberId)?.toDto()
//        val newItem = cartItemService.addOrUpdate(initial, member!!)
// //
// //
// //        val updated = CartItemRequestDTO(productId = productId, quantity = 5)
// //        val result = cartItemService.addOrUpdate(updated, member)
// //
//        assertThat(newItem.quantity).isEqualTo(1)
//    }

    @Test
    fun `addOrUpdate should throw if product not found`() {
        val badDto = CartItemRequestDTO(productId = 9999L, quantity = 1)
        val member = memberRepository.findByIdOrNull(memberId)?.toDto()

        assertThrows<EmptyResultDataAccessException> {
            cartItemService.addOrUpdate(badDto, member!!)
        }
    }

    @Test
    fun `findByMember should return cart items for a member`() {
        val member = memberRepository.findByIdOrNull(memberId)?.toDto()
        cartItemService.addOrUpdate(CartItemRequestDTO(productId, 3), member!!)

        val items = cartItemService.findByMember(memberId)

        assertThat(items).hasSize(1)
        assertThat(items[0].product.id).isEqualTo(productId)
        assertThat(items[0].quantity).isEqualTo(3)
    }

    @Test
    fun `delete should remove cart item for member`() {
        val member = memberRepository.findByIdOrNull(memberId)?.toDto()
        cartItemService.addOrUpdate(CartItemRequestDTO(productId, 2), member!!)

        cartItemService.delete(CartItemRequestDTO(productId, 2), memberId)

        val items = cartItemService.findByMember(memberId)
        assertThat(items).isEmpty()
    }
}
