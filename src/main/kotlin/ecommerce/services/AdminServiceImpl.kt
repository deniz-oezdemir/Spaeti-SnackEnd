package ecommerce.services

import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO
import ecommerce.repositories.CartItemRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class AdminServiceImpl(private val cartItemRepository: CartItemRepository) : AdminService {
    override fun findTopProductsAddedInList30Days(): List<TopProductDTO> {
        return cartItemRepository.findTop5ProductsAddedInLast30Days()
    }

    override fun findMembersWithRecentCartActivity(): List<ActiveMemberDTO> {
        return cartItemRepository.findDistinctMembersWithCartActivityInLast7Days()
    }
}
