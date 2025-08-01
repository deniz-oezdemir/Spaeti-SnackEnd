package ecommerce.services

import ecommerce.exception.NoSuchElementException
import ecommerce.mappers.toEntity
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.OptionDTO
import ecommerce.model.TopProductDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.ProductRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@Primary
class AdminServiceImpl(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) : AdminService {
    override fun findTopProductsAddedInList30Days(): List<TopProductDTO> {
        return cartItemRepository.findTop5ProductsAddedInLast30Days()
    }

    override fun findMembersWithRecentCartActivity(): List<ActiveMemberDTO> {
        return cartItemRepository.findDistinctMembersWithCartActivityInLast7Days()
    }

    override fun createOption(optionDTO: OptionDTO) {
        val product =
            productRepository.findByIdOrNull(optionDTO.productId!!)
                ?: throw NoSuchElementException("Product with id ${optionDTO.productId} doesn't exist")
        val option = optionDTO.toEntity(product)
        product.addOption(option)
        productRepository.save(product)
    }
}
