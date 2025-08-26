package ecommerce.service

import ecommerce.dto.OptionCreateDto
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OptionService(
    private val productRepositoryJpa: ProductRepositoryJpa,
    private val optionRepositoryJpa: OptionRepositoryJpa,
) {
    fun createProductWithOptions(
        name: String,
        price: Double,
        imageUrl: String,
        optionDtos: List<OptionCreateDto>,
    ): Product {
        val product = Product(name = name, price = price, imageUrl = imageUrl)
        product.options.addAll(
            optionDtos.map { dto ->
                Option(product = product, name = dto.name, quantity = dto.quantity)
            },
        )
        return productRepositoryJpa.save(product)
    }

    fun decreaseOptionQuantity(
        optionId: Long,
        amount: Long,
    ) {
        val option =
            optionRepositoryJpa.findWithLockById(optionId)
                ?: throw NoSuchElementException("Option not found id=$optionId")
        option.decreaseQuantity(amount)
        optionRepositoryJpa.saveAndFlush(option)
    }
}
