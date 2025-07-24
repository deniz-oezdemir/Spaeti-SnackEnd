package ecommerce.services

import ecommerce.model.CartItemRequestDTO
import ecommerce.model.CartItemResponseDTO

interface CartItemService {
    fun addOrUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): CartItemResponseDTO

    fun findByMember(memberId: Long): List<CartItemResponseDTO>

    fun delete(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    )
}
