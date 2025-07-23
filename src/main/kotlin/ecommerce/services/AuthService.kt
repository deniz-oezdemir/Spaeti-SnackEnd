package ecommerce.services

import ecommerce.exception.AuthorizationException
import ecommerce.infrastructure.JwtTokenProvider
import ecommerce.model.MemberDTO
import ecommerce.model.TokenRequestDTO
import ecommerce.model.TokenResponseDTO
import org.springframework.stereotype.Service

@Service
class AuthService(private val jwtTokenProvider: JwtTokenProvider, private val memberService: MemberService) {
    fun checkInvalidLogin(tokenRequestDTO: TokenRequestDTO): Boolean {
        val memberDTO = memberService.findByEmail(tokenRequestDTO.email)
        return tokenRequestDTO.email != memberDTO.email || tokenRequestDTO.password != memberDTO.password
    }

    fun findMemberByToken(token: String): MemberDTO {
        val payload = jwtTokenProvider.getPayload(token)
        return memberService.findByEmail(payload)
    }

    fun createToken(tokenRequestDTO: TokenRequestDTO): TokenResponseDTO {
        if (checkInvalidLogin(tokenRequestDTO)) throw AuthorizationException()

        val accessToken = jwtTokenProvider.createToken(tokenRequestDTO.email)
        return TokenResponseDTO(accessToken)
    }
}