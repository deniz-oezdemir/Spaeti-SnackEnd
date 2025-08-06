package ecommerce.model

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TokenRequestDTO(
    @field:NotBlank(message = ValidationMessages.EMAIL_BLANK)
    @field:NotNull(message = ValidationMessages.EMAIL_BLANK)
    @field:Email(message = ValidationMessages.EMAIL_INVALID)
    val email: String,
    @field:NotBlank(message = ValidationMessages.PASSWORD_BLANK)
    @field:NotNull(message = ValidationMessages.PASSWORD_BLANK)
    val password: String,
)
