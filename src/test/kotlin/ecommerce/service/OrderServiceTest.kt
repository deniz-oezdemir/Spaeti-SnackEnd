package ecommerce.service

import ecommerce.dto.PaymentRequest
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.StripeIntentResponse
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.enums.PaymentMethod
import ecommerce.infrastructure.StripeClient
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OrderItemRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class OrderServiceTest
    @Autowired
    constructor(
        private val productRepository: ProductRepositoryJpa,
        private val orderItemRepository: OrderItemRepositoryJpa,
        private val memberRepository: MemberRepositoryJpa,
        private val orderService: OrderService,
    ) {
        @MockitoBean
        private lateinit var stripeClient: StripeClient

        @BeforeEach
        fun stubStripe() {
            given(stripeClient.createAndConfirmPayment(any<PaymentRequest>()))
                .willReturn(StripeIntentResponse(id = "pi_test", status = "succeeded", clientSecret = "cs_test"))
        }

        @Test
        fun `persists order item with snapshot names`() {
            val member =
                memberRepository.save(
                    Member(name = "Jane", email = "jane@example.com", password = "pw", role = "USER"),
                )

            val product = Product(name = "Mug", price = 12.5, imageUrl = "https://img.example/mug.png")

            val option =
                Option(
                    product = product,
                    name = "Red",
                    quantity = 10,
                )

            product.options += option
            val savedProduct = productRepository.save(product)
            val optionId = savedProduct.options.first().id!!

            val req =
                PlaceOrderRequest(
                    optionId = optionId,
                    quantity = 2L,
                    paymentMethod = PaymentMethod.PM_CARD_VISA,
                    currency = "usd",
                )

            orderService.place(member, req)

            val items = orderItemRepository.findAll()
            val item = items.first()

            assertEquals("Mug", item.productName)
            assertEquals("Red", item.optionName)
            assertEquals(12.5, item.price)
            assertEquals(2, item.quantity)
        }
    }
