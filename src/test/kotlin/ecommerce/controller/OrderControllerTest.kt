package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.LoggedInMember
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.entity.Member
import ecommerce.enums.PaymentMethod
import ecommerce.enums.UserRole
import ecommerce.handler.GlobalExceptionHandler
import ecommerce.infrastructure.BearerAuthorizationExtractor
import ecommerce.infrastructure.JWTProvider
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.resolver.LoginMemberArgumentResolver
import ecommerce.service.AuthService
import ecommerce.service.MemberService
import ecommerce.service.OrderService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(OrderController::class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler::class)
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
        private lateinit var memberService: MemberService

        @MockitoBean
        private lateinit var memberRepository: MemberRepositoryJpa

        @MockitoBean
        private lateinit var bearerAuthorizationExtractor: BearerAuthorizationExtractor

        @MockitoBean(name = "loginMemberArgumentResolver")
        private lateinit var loginMemberArgumentResolver: LoginMemberArgumentResolver

        private val principal =
            LoggedInMember(id = 42L, name = "Jane", email = "jane@example.com", role = UserRole.USER.name)

        @BeforeEach
        fun stubLoginResolver() {
            `when`(loginMemberArgumentResolver.supportsParameter(any())).thenReturn(true)
            `when`(
                loginMemberArgumentResolver.resolveArgument(
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ).thenReturn(principal)

            given(bearerAuthorizationExtractor.extract(any())).willReturn("mock-token")
        }

        @Test
        fun `placeOrder - returns 201 with Location and response body`() {
            val persistedMember =
                Member(id = principal.id, name = "Jane", email = "jane@example.com", password = "pw", role = "USER")
            given(memberService.getByIdOrThrow(principal.id)).willReturn(persistedMember)

            val req = PlaceOrderRequest(optionId = 1001L, quantity = 2L, currency = "eur", paymentMethod = PaymentMethod.PM_CARD_VISA)
            val expected = PlaceOrderResponse(orderId = 555L, paymentStatus = "succeeded", message = "ok")

            given(orderService.place(any(), eq(req))).willReturn(expected)

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
        fun `placeOrder - service error returns 5xx with message`() {
            val persistedMember =
                Member(id = principal.id, name = "Jane", email = "jane@example.com", password = "pw", role = "USER")
            given(memberService.getByIdOrThrow(principal.id)).willReturn(persistedMember)

            val req =
                PlaceOrderRequest(
                    optionId = 1001L,
                    quantity = 3L,
                    currency = "eur",
                    paymentMethod = PaymentMethod.PM_CARD_CHARGE_DECLINED,
                )
            given(orderService.place(any(), eq(req))).willThrow(IllegalArgumentException("Payment not approved"))

            mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().is5xxServerError)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Payment not approved")))
        }

        @Test
        fun `placeOrder - when request body is invalid then 400`() {
            val badReq =
                """
                {
                  "optionId": 1001,
                  "quantity": 0,
                  "currency": "",
                  "paymentMethod": "pm_card_visa"
                }
                """.trimIndent()

            mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badReq),
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `placeOrder - when member not found then 404`() {
            val validBody =
                PlaceOrderRequest(
                    optionId = 1L,
                    quantity = 1L,
                    currency = "eur",
                    paymentMethod = PaymentMethod.PM_CARD_VISA,
                )

            whenever(memberService.getByIdOrThrow(principal.id))
                .thenThrow(NoSuchElementException("Member not found"))

            mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validBody)),
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `placeGiftOrder - returns 201 with Location and response body`() {
            val persistedMember =
                Member(id = principal.id, name = "Jane", email = "jane@example.com", password = "pw", role = "USER")
            given(memberService.getByIdOrThrow(principal.id)).willReturn(persistedMember)

            val req =
                ecommerce.dto.GiftCheckoutRequest(
                    recipientEmail = "friend@example.com",
                    message = "Happy Birthday! 🎉",
                    currency = "usd",
                    paymentMethod = PaymentMethod.PM_CARD_VISA,
                )
            val expected =
                PlaceOrderResponse(orderId = 777L, paymentStatus = "succeeded", message = "Gift order successful.")

            given(orderService.placeGift(any(), eq(req))).willReturn(expected)

            mockMvc.perform(
                post("/orders/gift")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().isCreated)
                .andExpect(header().string("Location", "/orders/${expected.orderId}"))
                .andExpect(jsonPath("$.orderId").value(expected.orderId))
                .andExpect(jsonPath("$.paymentStatus").value("succeeded"))
                .andExpect(jsonPath("$.message").value("Gift order successful."))
        }

        @Test
        fun `placeGiftOrder - when request body is invalid then 400`() {
            val badReq =
                ecommerce.dto.GiftCheckoutRequest(
                    recipientEmail = "not-an-email",
                    message = null,
                    currency = "usd",
                    paymentMethod = ecommerce.enums.PaymentMethod.PM_CARD_VISA,
                )

            mockMvc.perform(
                post("/orders/gift")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(badReq)),
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `placeGiftOrder - when member not found then 404`() {
            val req =
                ecommerce.dto.GiftCheckoutRequest(
                    recipientEmail = "friend@example.com",
                    message = null,
                    currency = "usd",
                    paymentMethod = PaymentMethod.PM_CARD_VISA,
                )

            whenever(memberService.getByIdOrThrow(principal.id))
                .thenThrow(NoSuchElementException("Member not found"))

            mockMvc.perform(
                post("/orders/gift")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `placeGiftOrder - service error returns 5xx with message`() {
            val persistedMember =
                Member(id = principal.id, name = "Jane", email = "jane@example.com", password = "pw", role = "USER")
            given(memberService.getByIdOrThrow(principal.id)).willReturn(persistedMember)

            val req =
                ecommerce.dto.GiftCheckoutRequest(
                    recipientEmail = "friend@example.com",
                    message = null,
                    currency = "usd",
                    paymentMethod = PaymentMethod.PM_CARD_CHARGE_DECLINED,
                )

            given(orderService.placeGift(any(), eq(req)))
                .willThrow(IllegalArgumentException("Payment not approved"))

            mockMvc.perform(
                post("/orders/gift")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req)),
            )
                .andExpect(status().is5xxServerError)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Payment not approved")))
        }
    }
