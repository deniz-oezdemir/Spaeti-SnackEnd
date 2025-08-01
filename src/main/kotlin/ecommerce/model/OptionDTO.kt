package ecommerce.model

import ecommerce.util.ValidationMessages.OPTION_NAME_PATTERN
import ecommerce.util.ValidationMessages.OPTION_NAME_REQUIRED
import ecommerce.util.ValidationMessages.OPTION_NAME_SIZE
import ecommerce.util.ValidationMessages.OPTION_PRODUCT_ID_REQUIRED
import ecommerce.util.ValidationMessages.OPTION_QUANTITY_SIZE
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class OptionDTO(
    val id: Long?,
    @field: NotBlank(message = OPTION_NAME_REQUIRED)
    @field:Size(min = 1, max = 50, message = OPTION_NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = OPTION_NAME_PATTERN)
    val name: String,
    @field:Size(min = 1, max = 99_999_999, message = OPTION_QUANTITY_SIZE)
    val quantity: Long,
    @field:NotBlank(message = OPTION_PRODUCT_ID_REQUIRED)
    val productId: Long? = null
)
