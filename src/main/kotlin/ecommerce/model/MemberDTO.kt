package ecommerce.model

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
    var password: String
) {
}