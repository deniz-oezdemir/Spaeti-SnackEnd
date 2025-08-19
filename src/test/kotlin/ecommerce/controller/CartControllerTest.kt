package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.CartRequest
import ecommerce.dto.MemberResponse
import ecommerce.entity.Cart
import ecommerce.enums.UserRole
import ecommerce.infrastructure.JWTProvider
import ecommerce.resolver.LoginMemberArgumentResolver
import ecommerce.service.AuthService
import ecommerce.service.CartService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.mockito.kotlin.any as ktAny

@WebMvcTest(CartController::class)
@AutoConfigureMockMvc
class CartControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var cartService: CartService

    @MockitoBean
    private lateinit var jwtProvider: JWTProvider

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    lateinit var loginMemberArgumentResolver: LoginMemberArgumentResolver

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val token = "mocked-jwt-token"
    private val memberResponse =
        MemberResponse(id = 1L, email = "user@example.com", name = "John Doe", role = UserRole.USER.name)
    private val cartRequest = CartRequest(productOptionId = 100L)

    @BeforeEach
    fun setup() {
        doNothing().`when`(jwtProvider).validateToken(token)
        `when`(authService.findMemberByToken(token)).thenReturn(memberResponse)

        `when`(loginMemberArgumentResolver.supportsParameter(ktAny())).thenReturn(true)
        `when`(
            loginMemberArgumentResolver.resolveArgument(
                ktAny(),
                ktAny(),
                ktAny(),
                ktAny(),
            ),
        ).thenReturn(memberResponse)

        val cartController = CartController(cartService)

        // Register it in MockMvc
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(cartController)
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .build()
    }

    @Test
    fun `should add product to cart`() {
        mockMvc.perform(
            post("/api/protected/cart/created")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)),
        )
            .andExpect(status().isCreated)

        verify(cartService).addToCart(memberResponse.id, cartRequest.productOptionId, quantity = 1)
    }

    @Test
    fun `should remove product from cart`() {
        mockMvc.perform(
            delete("/api/protected/cart/1")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)),
        )
            .andExpect(status().isNoContent)

        verify(cartService).removeFromCart(cartItemId = 1)
    }

    @Test
    fun `should return cart for authenticated member`() {
        // Given
        val cart = Cart(id = 100L, memberId = memberResponse.id)

        // Mock service
        `when`(cartService.getCart(memberResponse.id)).thenReturn(cart)

        // When & Then
        mockMvc.perform(
            get("/api/protected/cart")
                .header("Authorization", "Bearer $token")
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(cart.id))
            .andExpect(jsonPath("$.memberId").value(cart.memberId))

        verify(cartService, times(1)).getCart(memberResponse.id)
    }
}
