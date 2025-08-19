package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.MemberResponse
import ecommerce.dto.TokenRequest
import ecommerce.dto.TokenResponse
import ecommerce.enums.UserRole
import ecommerce.handler.AuthorizationException
import ecommerce.handler.ValidationException
import ecommerce.infrastructure.JWTProvider
import ecommerce.service.AuthService
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var jwtProvider: JWTProvider

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val tokenRequest =
        TokenRequest(name = "John Doe", "user@example.com", "password123", role = UserRole.USER.name)
    private val tokenResponse = TokenResponse("mocked-jwt-token")

    @Test
    fun `should successfully create new member`() {
        `when`(authService.register(tokenRequest)).thenReturn(tokenResponse)

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("mocked-jwt-token"))
    }

    @Test
    fun `should fail to create member if email already exists`() {
        `when`(authService.register(tokenRequest)).thenThrow(ValidationException("Email is already registered"))

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Email is already registered"))
    }

    @Test
    fun `should successfully login if email and password match`() {
        `when`(authService.createToken(tokenRequest)).thenReturn(tokenResponse)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("mocked-jwt-token"))
    }

    @Test
    fun `login should fail with incorrect password`() {
        `when`(authService.createToken(tokenRequest)).thenThrow(AuthorizationException("Invalid password for email"))

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid password for email"))
    }

    @Test
    fun `should get member if token exists`() {
        val token = "mocked-jwt-token"
        doNothing().`when`(jwtProvider).validateToken("Bearer $token")
        val memberResponse = MemberResponse(1L, "user@example.com", role = "user", name = "John Doe")
        `when`(authService.findMemberByToken(token)).thenReturn(memberResponse)

        mockMvc.perform(
            get("/auth/find-member")
                .header("Authorization", "Bearer mocked-jwt-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("user@example.com"))
    }

    @Test
    fun `shouldn't get member if token does not exist or invalid`() {
        `when`(jwtProvider.validateToken("invalid-token")).thenThrow(
            AuthorizationException(
                "Invalid or expired JWT token",
            ),
        )
        mockMvc.perform(
            get("/auth/find-member")
                .header("Authorization", "Bearer invalid-token"),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid or expired JWT token"))
    }
}
