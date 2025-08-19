package ecommerce.infrastructure

import ecommerce.handler.AuthorizationException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JWTProvider(
    @Value("\${security.jwt.token.secret-key}")
    private val secretKey: String,
    @Value("\${security.jwt.token.expire-time-millis}")
    private val validityInMillis: Long,
) {
    fun createToken(payload: String): String {
        val claims = Jwts.claims().setSubject(payload)
        val now = Date()
        val validity = Date(now.time + validityInMillis)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun getPayload(token: String): String =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .body
            .subject

    fun validateToken(token: String) {
        try {
            val claims =
                Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)

            if (claims.body.expiration.before(Date())) {
                throw AuthorizationException("JWT token expired")
            }
        } catch (ex: JwtException) {
            throw AuthorizationException("Invalid JWT token")
        } catch (ex: IllegalArgumentException) {
            throw AuthorizationException("Malformed JWT token")
        }
    }
}
