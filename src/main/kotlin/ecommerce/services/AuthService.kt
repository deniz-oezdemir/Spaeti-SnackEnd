package ecommerce.services

import ecommerce.exception.AuthorizationException
import ecommerce.infrastructure.JwtTokenProvider
import ecommerce.model.MemberDTO
import ecommerce.model.TokenResponseDTO
import org.springframework.stereotype.Service

@Service
class AuthService(private val jwtTokenProvider: JwtTokenProvider, private val memberService: MemberService) {
    fun checkInvalidLogin(requestMemberDTO: MemberDTO): Boolean {
        val memberDTO = memberService.findByEmail(requestMemberDTO.email)
        requestMemberDTO.copy(role = memberDTO.role)
        return requestMemberDTO.email != memberDTO.email || requestMemberDTO.password != memberDTO.password
    }

    fun findMemberByToken(token: String): MemberDTO {
        val (email, _) = jwtTokenProvider.getPayload(token)
        return memberService.findByEmail(email)
    }

    fun createToken(memberDTO: MemberDTO): TokenResponseDTO {
        if (checkInvalidLogin(memberDTO)) throw AuthorizationException()

        val accessToken = jwtTokenProvider.createToken(memberDTO.email, memberDTO.role)
        return TokenResponseDTO(accessToken)
    }
}
