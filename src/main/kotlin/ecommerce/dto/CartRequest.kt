package ecommerce.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

class CartRequest(
    val productOptionId: Long? = null,
    @field:Size(min = 1, max = 200)
    val productName: String? = null,
    @field:Size(min = 1, max = 50)
    val optionName: String? = null,
    @field:Min(1)
    val quantity: Long = 1,
) {
    fun hasId(): Boolean = productOptionId != null

    fun hasNames(): Boolean = !productName.isNullOrBlank() && !optionName.isNullOrBlank()
}
