package ecommerce.dto

data class TokenRequest(
    val name: String,
    var email: String,
    var password: String,
    val role: String,
    val slackUserId: String? = null,
)
