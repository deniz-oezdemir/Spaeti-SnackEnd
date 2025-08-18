package ecommerce.dto

data class LoggedInMember(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
)
