package ecommerce.model

import ecommerce.entities.Member
import ecommerce.util.ValidationMessages.EMAIL_BLANK
import ecommerce.util.ValidationMessages.EMAIL_INVALID
import ecommerce.util.ValidationMessages.PASSWORD_BLANK
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MemberDTO(
    var id: Long? = null,

    @field:NotBlank(message = EMAIL_BLANK)
    @field:Email(message = EMAIL_INVALID)
    var email: String,

    @field:NotBlank(message = PASSWORD_BLANK)
    var password: String,

    var role: Member.Role = Member.Role.CUSTOMER,
)
