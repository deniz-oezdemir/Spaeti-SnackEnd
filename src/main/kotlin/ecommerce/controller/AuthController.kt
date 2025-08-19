package ecommerce.controller

import ecommerce.dto.MemberResponse
import ecommerce.dto.TokenRequest
import ecommerce.dto.TokenResponse
import ecommerce.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(
        @RequestBody request: TokenRequest,
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.register(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: TokenRequest,
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.createToken(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @GetMapping("/find-member")
    fun findMember(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<MemberResponse> {
        val token = authHeader.removePrefix("Bearer ").trim()
        val memberResponse = authService.findMemberByToken(token)
        return ResponseEntity.ok(memberResponse)
    }
}
