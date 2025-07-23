package ecommerce.model

import ecommerce.entities.Member
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MemberDTO(
    var id: Long? = null,
    @field:NotBlank
    @field:NotNull
    @field:Email
    var email: String,
    @field:NotBlank
    @field:NotNull
    var password: String,
    var role: Member.Role = Member.Role.CUSTOMER,
)
