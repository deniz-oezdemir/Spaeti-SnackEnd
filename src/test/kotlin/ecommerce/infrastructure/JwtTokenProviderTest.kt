package ecommerce.infrastructure

import ecommerce.entities.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Base64

class JwtTokenProviderTest {
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val secretKey = Base64.getEncoder().encodeToString("test-secret-key-test-secret-key".toByteArray())
    private val expirationTime = 1000L // 1 second

    @BeforeEach
    fun setup() {
        jwtTokenProvider = JwtTokenProvider(secretKey, expirationTime)
    }

    @Test
    fun `should create a valid token`() {
        val token = jwtTokenProvider.createToken("123", Member.Role.CUSTOMER)

        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateToken(token)).isTrue()
    }

    @Test
    fun `should extract correct payload and role from token`() {
        val token = jwtTokenProvider.createToken("42", Member.Role.ADMIN)

        val (payload, role) = jwtTokenProvider.getPayload(token)

        assertThat(payload).isEqualTo("42")
        assertThat(role).isEqualTo(Member.Role.ADMIN)
    }

    @Test
    fun `should return false for invalid token`() {
        val invalidToken = "invalid.token.value"

        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse()
    }

    @Test
    fun `should return false for expired token`() {
        val shortLivedProvider = JwtTokenProvider(secretKey, 1) // 1 ms
        val token = shortLivedProvider.createToken("expired", Member.Role.CUSTOMER)
        Thread.sleep(5)

        assertThat(shortLivedProvider.validateToken(token)).isFalse()
    }
}
