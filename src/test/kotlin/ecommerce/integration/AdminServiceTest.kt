package ecommerce.integration

import ecommerce.entities.Member
import ecommerce.entities.Product
import ecommerce.mappers.toDto
import ecommerce.model.CartItemRequestDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.ProductRepository
import ecommerce.services.AdminServiceImpl
import ecommerce.services.CartItemServiceImpl
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Transactional
class AdminServiceTest {
    @Autowired
    private lateinit var adminService: AdminServiceImpl

    @Autowired
    private lateinit var cartItemService: CartItemServiceImpl

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    private lateinit var member1: Member
    private lateinit var member2: Member
    private lateinit var product1: Product
    private lateinit var product2: Product

    @BeforeEach
    fun setup() {
        cartItemRepository.deleteAll()
        memberRepository.deleteAll()
        productRepository.deleteAll()

        member1 = memberRepository.save(Member(name = "m1", email = "m1@example.com", password = "pw"))!!
        member2 = memberRepository.save(Member(name = "m2", email = "m2@example.com", password = "pw"))!!

        product1 = productRepository.save(Product(name = "Mouse", price = 10.0, imageUrl = "mouse.jpg"))!!
        product2 = productRepository.save(Product(name = "Keyboard", price = 20.0, imageUrl = "keyboard.jpg"))!!

        cartItemService.addOrUpdate(CartItemRequestDTO(product1.id!!, 1), member1.toDto())
        cartItemService.addOrUpdate(CartItemRequestDTO(product2.id!!, 2), member1.toDto())
        cartItemService.addOrUpdate(CartItemRequestDTO(product2.id!!, 1), member2.toDto())
    }

    @Test
    fun `findTopProductsAddedInList30Days returns top products`() {
        val result = adminService.findTopProductsAddedInList30Days()

        assertThat(result).isNotEmpty
        assertThat(result.map { it.name }).contains("Mouse", "Keyboard")
    }

    @Test
    fun `findMembersWithRecentCartActivity returns distinct members`() {
        val result = adminService.findMembersWithRecentCartActivity()

        assertThat(result.map { it.email }).containsExactlyInAnyOrder("m1@example.com", "m2@example.com")
    }
}
