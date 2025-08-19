package ecommerce.validation

import ecommerce.repository.ProductRepositoryJpa
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Component

@Component
class UniqueProductNameValidator(
    private val productRepositoryJpa: ProductRepositoryJpa,
) : ConstraintValidator<UniqueProductName, String> {
    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return productRepositoryJpa.findByName(value) == null
    }
}
