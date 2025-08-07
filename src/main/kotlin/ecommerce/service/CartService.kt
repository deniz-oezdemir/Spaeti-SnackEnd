package ecommerce.service

import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import ecommerce.entity.Cart
import ecommerce.entity.CartItem
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CartService(
    private val productRepositoryJpa: ProductRepositoryJpa,
    private val cartItemRepositoryJpa: CartItemRepositoryJpa,
    private val cartRepositoryJpa: CartRepositoryJpa,
) {
    fun addToCart(
        memberId: Long,
        productId: Long,
    ) {
        val cart = cartRepositoryJpa.findCartByMemberId(memberId) ?: throw NoSuchElementException("Cart not found")
        val product =
            productRepositoryJpa.findById(productId).orElse(null)
                ?: throw NoSuchElementException("Product not found")

        val carItemEntity =
            CartItem(
                product = product,
                cart = cart,
                quantity = 1,
            )

        cartItemRepositoryJpa.save(carItemEntity)
    }

    fun removeFromCart(cartItemId: Long) {
        return cartItemRepositoryJpa.deleteById(cartItemId)
    }

    fun getCart(memberId: Long): Cart {
        return cartRepositoryJpa.findCartByMemberId(memberId) ?: throw NoSuchElementException("Cart not found")
    }

    fun findTop5ProductsInLast30Days(): List<TopProductStatResponse> {
        return cartItemRepositoryJpa.findTop5ProductsInLast30Days()
    }

    fun findMembersWithCartActivityInLast7Days(): List<MemberResponse> {
        return cartRepositoryJpa.findMembersWithCartActivityInLast7Days()
    }

    fun getCartItems(
        memberId: Long,
        page: Int,
        size: Int,
        sortBy: String = "created_at",
        direction: Sort.Direction = Sort.Direction.ASC,
    ): Page<CartItem> {
        val cart = cartRepositoryJpa.findCartByMemberId(memberId) ?: throw NoSuchElementException("Cart not found")
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        return cartItemRepositoryJpa.findAllByCartId(cartId = cart.id!!, pageable)
    }

    fun getItemsByQuantity(
        quantity: Int,
        page: Int,
        size: Int,
    ): Page<CartItem> {
        val pageable = PageRequest.of(page, size)
        return cartItemRepositoryJpa.findAllByQuantity(quantity, pageable)
    }
}
