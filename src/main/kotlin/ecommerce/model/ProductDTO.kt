package ecommerce.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductDTO(
    var id: Long? = null,
    @field:NotBlank
    @field:NotNull
    @field:Size(min = 1, max = 15, message = "The product name must contain between 1 and 15 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9()\\[\\]+\\-&/_]*$", message = "Invalid characters in product name.")
    var name: String,
    @field:NotNull
    @field:Positive
    var price: Double,
    @field:NotBlank
    @field:NotNull
    @field:Pattern(regexp = "^https?://.*$", message = "Invalid imageUrl, should start with http:// or https://")
    var imageUrl: String,
) {
    fun copyFrom(productDTO: ProductDTO): ProductDTO {
        productDTO.id?.let { this.id = it }
        this.name = productDTO.name
        this.price = productDTO.price
        this.imageUrl = productDTO.imageUrl
        return this
    }

    fun copyFrom(productPatchDTO: ProductPatchDTO): ProductDTO {
        productPatchDTO.id?.let { this.id = it }
        productPatchDTO.name?.takeIf { it.isNotBlank() }?.let { this.name = it }
        productPatchDTO.price?.let { this.price = it }
        productPatchDTO.imageUrl?.takeIf { it.isNotBlank() }?.let { this.imageUrl = it }
        return this
    }
}
