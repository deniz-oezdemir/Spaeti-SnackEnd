package ecommerce.model

import ecommerce.util.ValidationMessages.IMAGE_FORMAT
import ecommerce.util.ValidationMessages.IMAGE_REQUIRED
import ecommerce.util.ValidationMessages.NAME_PATTERN
import ecommerce.util.ValidationMessages.NAME_REQUIRED
import ecommerce.util.ValidationMessages.NAME_SIZE
import ecommerce.util.ValidationMessages.PRICE_POSITIVE
import ecommerce.util.ValidationMessages.PRICE_REQUIRED
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductDTO(
    var id: Long? = null,
    @field:NotBlank(NAME_REQUIRED)
    @field:Size(min = 1, max = 15, message = NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = NAME_PATTERN)
    var name: String,
    @field:NotNull(PRICE_REQUIRED)
    @field:Positive(PRICE_POSITIVE)
    var price: Double,
    @field:NotBlank(IMAGE_REQUIRED)
    @field:Pattern(regexp = "^https?://.*$", message = IMAGE_FORMAT)
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
