package ecommerce.model

import ecommerce.util.ValidationMessages.IMAGE_FORMAT
import ecommerce.util.ValidationMessages.IMAGE_REQUIRED
import ecommerce.util.ValidationMessages.NAME_PATTERN
import ecommerce.util.ValidationMessages.NAME_REQUIRED
import ecommerce.util.ValidationMessages.OPTION_REQUIRED
import ecommerce.util.ValidationMessages.PRICE_POSITIVE
import ecommerce.util.ValidationMessages.PRICE_REQUIRED
import ecommerce.util.ValidationMessages.PRODUCT_NAME_SIZE
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductRequestDTO(
    var id: Long? = null,
    @field:NotBlank(message = NAME_REQUIRED)
    @field:Size(min = 1, max = 15, message = PRODUCT_NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = NAME_PATTERN)
    var name: String,
    @field:NotNull(message = PRICE_REQUIRED)
    @field:Positive(message = PRICE_POSITIVE)
    var price: Double,
    @field:NotBlank(message = IMAGE_REQUIRED)
    @field:Pattern(regexp = "^https?://.*$", message = IMAGE_FORMAT)
    var imageUrl: String,
    @field:NotEmpty(message = OPTION_REQUIRED)
    val options: Set<OptionDTO>,
)
