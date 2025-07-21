package ecommerce.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductPatchDTO(
    var id: Long? = null,
    @field:NotBlank
    @field:Size(min = 1, max = 15, message = "The product name must contain between 1 and 15 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = "Invalid characters in product name.")
    var name: String? = null,
    @field:Positive
    var price: Double? = null,
    @field:NotBlank
    @field:Pattern(regexp = "^https?://.*$", message = "Invalid imageUrl, should start with http:// or https://")
    var imageUrl: String? = null,
)
