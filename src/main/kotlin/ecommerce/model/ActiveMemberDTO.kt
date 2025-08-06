package ecommerce.model

import ecommerce.util.ValidationMessages.EMAIL_INVALID
import ecommerce.util.ValidationMessages.MEMBER_NAME_REQUIRED
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ActiveMemberDTO(
    val id: Long,
    @field:NotBlank(message = MEMBER_NAME_REQUIRED)
    val name: String,
    @field:Email(message = EMAIL_INVALID)
    val email: String,
)
