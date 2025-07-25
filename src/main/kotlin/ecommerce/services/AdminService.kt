package ecommerce.services

import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO

interface AdminService {
    fun findTopProductsAddedInList30Days(): List<TopProductDTO>

    fun findMembersWithRecentCartActivity(): List<ActiveMemberDTO>
}
