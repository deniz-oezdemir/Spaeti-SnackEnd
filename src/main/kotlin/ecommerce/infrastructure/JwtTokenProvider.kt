package ecommerce.infrastructure

import ecommerce.entities.Member
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${security.jwt.token.secret-key}")
    secret: String,
    @Value("\${security.jwt.token.expire-length}")
    private val validityInMs: Long,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun createToken(
        payload: String,
        role: Member.Role,
    ): String {
        val now = Date()
        val exp = Date(now.time + validityInMs)
        return Jwts.builder()
            .subject(payload)
            .claim("role", role)
            .issuedAt(now)
            .expiration(exp)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun getPayload(token: String): Pair<String, Member.Role> {
        val claims =
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        val subject = claims.subject
        val role = Member.Role.valueOf(claims["role"] as String)

        return Pair(subject, role)
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
