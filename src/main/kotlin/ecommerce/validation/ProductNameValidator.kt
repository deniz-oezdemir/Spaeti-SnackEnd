package ecommerce.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ProductNameValidator : ConstraintValidator<ValidProductName, String> {
    private val regex = Regex("^[a-zA-Z0-9\\s()\\[\\]+\\-&/_]*$")

    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return regex.matches(value)
    }
}
