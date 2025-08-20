package ecommerce.service

import ecommerce.dto.MemberResponse
import ecommerce.dto.TokenRequest
import ecommerce.dto.TokenResponse
import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.handler.AuthorizationException
import ecommerce.handler.ValidationException
import ecommerce.infrastructure.JWTProvider
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.MemberRepositoryJpa
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtTokenProvider: JWTProvider,
    private val memberRepository: MemberRepositoryJpa,
    private val cartRepository: CartRepositoryJpa,
) {
    fun createToken(tokenRequest: TokenRequest): TokenResponse {
        val member =
            memberRepository.findByEmail(tokenRequest.email)
                ?: throw AuthorizationException("Member not found with email: ${tokenRequest.email}")

        if (member.password != tokenRequest.password) {
            throw AuthorizationException("Invalid password for email: ${tokenRequest.email}")
        }

        val accessToken = jwtTokenProvider.createToken(member.email)
        return TokenResponse(accessToken)
    }

    fun register(tokenRequest: TokenRequest): TokenResponse {
        if (memberRepository.existsByEmail(tokenRequest.email)) {
            throw ValidationException("Email is already registered")
        }

        val role = if (tokenRequest.email == "admin@example.com") "ADMIN" else "USER"

        val slackUserId = tokenRequest.slackUserId
        println(tokenRequest.slackUserId)

        val member =
            memberRepository.save(
                Member(
                    name = tokenRequest.name,
                    email = tokenRequest.email,
                    password = tokenRequest.password,
                    role = role,
                    slackUserId = slackUserId,
                ),
            )

        val newCart = Cart(memberId = member.id!!)
        cartRepository.save(newCart)

        val accessToken = jwtTokenProvider.createToken(member.email)
        return TokenResponse(accessToken)
    }

    fun findMemberByToken(token: String): MemberResponse {
        jwtTokenProvider.validateToken(token)
        val email = jwtTokenProvider.getPayload(token)

        val member =
            memberRepository.findByEmail(email)
                ?: throw AuthorizationException("Member not found with email: $email")

        return MemberResponse(
            id = member.id!!,
            email = member.email,
            role = member.role,
            name = member.name,
            slackUserId = member.slackUserId,
        )
    }
}
