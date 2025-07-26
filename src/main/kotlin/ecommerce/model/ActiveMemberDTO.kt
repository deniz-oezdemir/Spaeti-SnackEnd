package ecommerce.model

import ecommerce.util.ValidationMessages.EMAIL_INVALID
import jakarta.validation.constraints.Email

data class ActiveMemberDTO(
    val id: Long,
    @field:Email(message = EMAIL_INVALID)
    val email: String,
)
