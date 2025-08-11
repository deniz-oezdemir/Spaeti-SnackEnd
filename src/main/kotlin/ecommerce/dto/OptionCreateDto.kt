package ecommerce.dto

import ecommerce.entity.Option
import ecommerce.entity.Product
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class OptionCreateDto(
    @field:Size(max = 50)
    @field:Pattern(
        regexp = "^[\\p{L}\\p{N}\\s()\\[\\]+\\-&/_]*\$",
        message = "Invalid characters in option name",
    )
    val name: String,
    @field:Min(1) @field:Max(99_999_999)
    val quantity: Long,
) {
    fun toOption(product: Product): Option {
        return Option(
            product = product,
            name = name,
            quantity = quantity,
        )
    }
}
