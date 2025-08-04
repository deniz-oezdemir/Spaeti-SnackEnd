package ecommerce.services

import ecommerce.exception.MissingProductIdException
import ecommerce.exception.NoSuchElementException
import ecommerce.mappers.toEntity
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.OptionDTO
import ecommerce.model.TopProductDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.ProductRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class AdminServiceImpl(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) : AdminService {
    @Transactional(readOnly = true)
    override fun findTopProductsAddedInList30Days(): List<TopProductDTO> {
        return cartItemRepository.findTop5ProductsAddedInLast30Days()
    }

    @Transactional(readOnly = true)
    override fun findMembersWithRecentCartActivity(): List<ActiveMemberDTO> {
        return cartItemRepository.findDistinctMembersWithCartActivityInLast7Days()
    }

    @Transactional
    override fun createOption(optionDTO: OptionDTO) {
        val productId = optionDTO.productId
            ?: throw MissingProductIdException("productId is required when creating an option standalone")
        val product =
            productRepository.findByIdOrNull(productId)
                ?: throw NoSuchElementException("Product with id $productId doesn't exist")
        val option = optionDTO.toEntity(product)
        product.addOption(option)
        productRepository.save(product)
    }
}
