package ecommerce.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TokenRequestDTO(
    @field:NotBlank
    @field:NotNull
    @field:Email
    val email: String,
    @field:NotBlank
    @field:NotNull
    val password: String,
)
