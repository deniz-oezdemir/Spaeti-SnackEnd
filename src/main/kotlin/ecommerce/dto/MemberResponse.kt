package ecommerce.dto

import jakarta.validation.constraints.Email

class MemberResponse(
    val id: Long,
    @field:Email(message = "Invalid email")
    val email: String,
    val name: String,
    val role: String,
    val slackUserId: String? = null,
)
