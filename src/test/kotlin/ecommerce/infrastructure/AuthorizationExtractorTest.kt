package ecommerce.infrastructure

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Collections

class AuthorizationExtractorTest {
    private val extractor = AuthorizationExtractor()

    @Test
    fun `should extract token from valid bearer header`() {
        val request = mock(HttpServletRequest::class.java)
        val headers = Collections.enumeration(listOf("Bearer some-token"))
        `when`(request.getHeaders("Authorization")).thenReturn(headers)

        val token = extractor.extractToken(request)

        assertThat(token).isEqualTo("some-token")
    }

    @Test
    fun `should extract token and ignore comma suffix`() {
        val request = mock(HttpServletRequest::class.java)
        val headers = Collections.enumeration(listOf("Bearer some-token,extra"))
        `when`(request.getHeaders("Authorization")).thenReturn(headers)

        val token = extractor.extractToken(request)

        assertThat(token).isEqualTo("some-token")
    }

    @Test
    fun `should return empty string when no bearer token`() {
        val request = mock(HttpServletRequest::class.java)
        val headers = Collections.enumeration(listOf("Basic some-other-token"))
        `when`(request.getHeaders("Authorization")).thenReturn(headers)

        val token = extractor.extractToken(request)

        assertThat(token).isEmpty()
    }

    @Test
    fun `should return empty string when no Authorization header`() {
        val request = mock(HttpServletRequest::class.java)
        val headers = Collections.emptyEnumeration<String>()
        `when`(request.getHeaders("Authorization")).thenReturn(headers)

        val token = extractor.extractToken(request)

        assertThat(token).isEmpty()
    }
}
