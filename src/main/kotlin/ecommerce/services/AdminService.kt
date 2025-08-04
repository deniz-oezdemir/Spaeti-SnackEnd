package ecommerce.services

import ecommerce.exception.MissingProductIdException
import ecommerce.mappers.toEntity
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.OptionDTO
import ecommerce.model.TopProductDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) { // Removed ": AdminService" and @Primary

    @Transactional(readOnly = true)
    fun findTopProductsAddedInList30Days(): List<TopProductDTO> {
        return cartItemRepository.findTop5ProductsAddedInLast30Days()
    }

    @Transactional(readOnly = true)
    fun findMembersWithRecentCartActivity(): List<ActiveMemberDTO> {
        return cartItemRepository.findDistinctMembersWithCartActivityInLast7Days()
    }

    @Transactional
    fun createOption(optionDTO: OptionDTO) {
        val productId =
            optionDTO.productId
                ?: throw MissingProductIdException("productId is required when creating an option standalone")
        val product =
            productRepository.findByIdOrNull(productId)
                ?: throw NoSuchElementException("Product with id $productId doesn't exist")
        val option = optionDTO.toEntity(product)
        product.addOption(option)
        productRepository.save(product)
    }
}
