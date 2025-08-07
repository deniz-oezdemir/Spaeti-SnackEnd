package ecommerce.validation

import ecommerce.repository.ProductRepositoryJpa
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Qualifier

class UniqueProductNameValidatorTest {
    @Qualifier("jdbcProductStore")
    private lateinit var productRepositoryJpa: ProductRepositoryJpa
    private lateinit var validator: UniqueProductNameValidator

    @BeforeEach
    fun setup() {
        productRepositoryJpa = mock(ProductRepositoryJpa::class.java)
        validator = UniqueProductNameValidator(productRepositoryJpa)
    }

    @Test
    fun `should return true when product name does not exist`() {
        `when`(productRepositoryJpa.findByName("New Product")).thenReturn(null)

        val result = validator.isValid("New Product", null)

        assertThat(result).isTrue()
    }
}
