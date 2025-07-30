package ecommerce.services

import ecommerce.model.CartItemRequestDTO
import ecommerce.model.CartItemResponseDTO
import ecommerce.model.MemberDTO

interface CartItemService {
    fun addOrUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        member: MemberDTO,
    ): CartItemResponseDTO

    fun findByMember(memberId: Long): List<CartItemResponseDTO>

    fun delete(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    )

    fun deleteAll()
}
