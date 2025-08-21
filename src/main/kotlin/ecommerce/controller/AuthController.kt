package ecommerce.controller

import ecommerce.dto.MemberResponse
import ecommerce.dto.TokenRequest
import ecommerce.dto.TokenResponse
import ecommerce.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "APIs for user registration and login")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User registered successfully"),
            ApiResponse(responseCode = "400", description = "Validation error, e.g., email already exists"),
        ],
    )
    @PostMapping("/register")
    fun register(
        @RequestBody request: TokenRequest,
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.register(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @Operation(summary = "Log in a user", description = "Authenticates a user and returns a new JWT.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Login successful"),
            ApiResponse(responseCode = "401", description = "Invalid email or password"),
        ],
    )
    @PostMapping("/login")
    fun login(
        @RequestBody request: TokenRequest,
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.createToken(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @Operation(summary = "Get current user's info", description = "Retrieves the logged-in user's details using their JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User details found"),
            ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        ],
    )
    @GetMapping("/find-member")
    fun findMember(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<MemberResponse> {
        val token = authHeader.removePrefix("Bearer ").trim()
        val memberResponse = authService.findMemberByToken(token)
        return ResponseEntity.ok(memberResponse)
    }
}
