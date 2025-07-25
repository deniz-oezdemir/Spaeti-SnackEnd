package ecommerce.repositories

import ecommerce.entities.CartItem
import ecommerce.entities.Product
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO

interface CartItemRepository {
    fun create(cartItem: CartItem): Pair<CartItem, Product>?

    fun update(cartItem: CartItem): Pair<CartItem, Product>?

    fun findByMember(memberId: Long): List<Pair<CartItem, Product>>

    fun existsByProductAndMember(
        productId: Long,
        memberId: Long,
    ): Boolean

    fun deleteByProduct(cartItem: CartItem): Boolean

    fun findTop5ProductsAddedInLast30Days(): List<TopProductDTO>

    fun findDistinctMembersWithCartActivityInLast7Days(): List<ActiveMemberDTO>

    fun deleteAll()
}
