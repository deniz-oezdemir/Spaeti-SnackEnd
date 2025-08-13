package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.MemberResponse
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.entity.Member
import ecommerce.enums.UserRole
import ecommerce.infrastructure.JWTProvider
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.resolver.LoginMemberArgumentResolver
import ecommerce.service.AuthService
import ecommerce.service.OrderService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional
import org.mockito.kotlin.any as ktAny

@WebMvcTest(OrderController::class)
@AutoConfigureMockMvc
class OrderControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) {
        @MockitoBean
        private lateinit var orderService: OrderService

        @MockitoBean
        private lateinit var jwtProvider: JWTProvider

        @MockitoBean
        private lateinit var authService: AuthService

        @MockitoBean
        private lateinit var memberRepository: MemberRepositoryJpa

        @MockitoBean(name = "loginMemberArgumentResolver")
        private lateinit var loginMemberArgumentResolver: LoginMemberArgumentResolver

        private val memberResponse =
            MemberResponse(id = 42L, name = "Jane", email = "jane@example.com", role = UserRole.USER.name)

        @BeforeEach
        fun stubLoginResolver() {
            `when`(loginMemberArgumentResolver.supportsParameter(ktAny())).thenReturn(true)
            `when`(
                loginMemberArgumentResolver.resolveArgument(
                    ktAny(),
                    ktAny(),
                    ktAny(),
                    ktAny(),
                ),
            ).thenReturn(memberResponse)
        }

        @Test
        fun `placeOrder - returns 201 with Location and response body`() {
            val memberId = 42L
            val member = Member(id = memberId, name = "Jane", email = "jane@example.com", password = "pw", role = "USER")
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

            val req = PlaceOrderRequest(optionId = 1001L, quantity = 2L, currency = "usd", paymentMethod = "pm_card_visa")
            val expected = PlaceOrderResponse(orderId = 555L, paymentStatus = "succeeded", message = "ok")
            given(orderService.place(member, req)).willReturn(expected)

            mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().isCreated)
                .andExpect(header().string("Location", "/orders/${expected.orderId}"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(expected.orderId))
                .andExpect(jsonPath("$.paymentStatus").value("succeeded"))
                .andExpect(jsonPath("$.message").value("ok"))
        }

        @Test
        fun `placeOrder - member not found returns 400 and does not call service`() {
            given(memberRepository.findById(42L)).willReturn(Optional.empty())

            val req = PlaceOrderRequest(optionId = 1001L, quantity = 1L, currency = "usd", paymentMethod = "pm_card_visa")

            mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().isBadRequest)

            verifyNoInteractions(orderService)
        }

        @Test
        fun `placeOrder - service error returns 400 with message`() {
            val memberId = 42L
            val member = Member(id = memberId, name = "Jane", email = "jane@example.com", password = "pw", role = "USER")
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

            val req =
                PlaceOrderRequest(
                    optionId = 1001L,
                    quantity = 3L,
                    currency = "usd",
                    paymentMethod = "pm_card_chargeDeclined",
                )
            given(orderService.place(member, req)).willThrow(IllegalArgumentException("Payment not approved"))

            mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().is5xxServerError)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Payment not approved")))
        }
    }
