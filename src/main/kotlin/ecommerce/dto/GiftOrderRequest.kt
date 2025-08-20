package ecommerce.dto

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class GiftOrderRequest(
    @field:Email @field:NotBlank
    val userEmail: String,

    @field:Email @field:NotBlank
    val recipientEmail: String,

    @field:NotEmpty
    @JsonAlias("productIds")
    val productOptionIds: List<Long>,

    val message: String? = null
)
