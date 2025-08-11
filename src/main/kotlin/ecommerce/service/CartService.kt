package ecommerce.service

import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import ecommerce.entity.Cart
import ecommerce.entity.CartItem
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartItemRepositoryJpa: CartItemRepositoryJpa,
    private val cartRepositoryJpa: CartRepositoryJpa,
    private val optionRepositoryJpa: OptionRepositoryJpa,
) {
    fun addToCart(
        memberId: Long,
        productOptionId: Long,
        quantity: Long,
    ) {
        val cart = cartRepositoryJpa.findCartByMemberId(memberId) ?: throw NoSuchElementException("Cart not found")
        val cartId = cart.id
        val option =
            optionRepositoryJpa.findById(productOptionId).orElse(null)
                ?: throw NoSuchElementException("Product Option not found")

        val existing =
            cartItemRepositoryJpa
                .findByCartIdAndProductOptionId(cartId!!, option.id!!)

        if (existing != null) {
            existing.quantity += quantity
            cartItemRepositoryJpa.save(existing)
        } else {
            val item =
                CartItem(
                    cart = cart,
                    productOption = option,
                    quantity = quantity,
                )
            cartItemRepositoryJpa.save(item)
        }
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
        quantity: Long,
        page: Int,
        size: Int,
    ): Page<CartItem> {
        val pageable = PageRequest.of(page, size)
        return cartItemRepositoryJpa.findAllByQuantity(quantity, pageable)
    }
}
