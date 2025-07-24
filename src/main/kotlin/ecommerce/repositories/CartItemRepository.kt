package ecommerce.repositories

import ecommerce.entities.CartItem
import ecommerce.entities.Product

interface CartItemRepository {
    fun create(cartItem: CartItem): Pair<CartItem, Product>?

    fun update(cartItem: CartItem): Pair<CartItem, Product>?

    fun findByMember(memberId: Long): List<Pair<CartItem, Product>>

    fun existsByProduct(productId: Long): Boolean

    fun deleteByProduct(cartItem: CartItem): Boolean
}
