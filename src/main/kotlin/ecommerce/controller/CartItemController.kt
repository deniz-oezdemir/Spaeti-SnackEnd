package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.model.CartItemRequestDTO
import ecommerce.model.CartItemResponseDTO
import ecommerce.model.MemberDTO
import ecommerce.services.CartItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/carts")
class CartItemController(private val cartItemService: CartItemService) {
    @GetMapping
    fun getCartItemsWithProducts(
        @LoginMember member: MemberDTO,
    ): ResponseEntity<List<CartItemResponseDTO>> {
        val cartItems = cartItemService.findByMember(member.id!!)
        return ResponseEntity.ok().body(cartItems)
    }

    @PostMapping
    fun addOrUpdateCartItem(
        @RequestBody cartItemRequestDTO: CartItemRequestDTO,
        @LoginMember member: MemberDTO,
    ): ResponseEntity<CartItemResponseDTO> {
        val cartItemResponseDTO = cartItemService.addOrUpdate(cartItemRequestDTO, member)
        return ResponseEntity.ok().body(cartItemResponseDTO)
    }

    @DeleteMapping
    fun deleteCartItem(
        @RequestBody cartItemRequestDTO: CartItemRequestDTO,
        @LoginMember member: MemberDTO,
    ): ResponseEntity<Unit> {
        cartItemService.delete(cartItemRequestDTO, member.id!!)
        return ResponseEntity.noContent().build()
    }
}
