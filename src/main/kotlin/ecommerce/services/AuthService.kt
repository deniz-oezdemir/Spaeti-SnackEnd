package ecommerce.services

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
        val (email, _) = jwtTokenProvider.getPayload(token)
        return memberService.findByEmail(email)
    }

    fun createToken(memberDTO: MemberDTO): TokenResponseDTO {
        val accessToken = jwtTokenProvider.createToken(memberDTO.email, memberDTO.role)
        return TokenResponseDTO(accessToken)
    }
}
