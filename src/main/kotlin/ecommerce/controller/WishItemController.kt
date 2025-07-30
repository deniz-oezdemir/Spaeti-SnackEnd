package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.model.WishItemResponseDTO
import ecommerce.model.MemberDTO
import ecommerce.model.WishItemRequestDTO
import ecommerce.services.WishItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wish")
class WishItemController(private val wishItemService: WishItemService) {
    @GetMapping
    fun getCartItemsWithProducts(
            @LoginMember member: MemberDTO,
            ): ResponseEntity<List<WishItemResponseDTO>> {
        val cartItems = wishItemService.findByMember(member.id!!)
        return ResponseEntity.ok().body(cartItems)
    }

    @PostMapping
    fun saveCartItem(
        @RequestBody wishItemRequestDTO: WishItemRequestDTO,
        @LoginMember member: MemberDTO,
            ): ResponseEntity<WishItemResponseDTO> {
        val WishItemResponseDTO = wishItemService.save(wishItemRequestDTO, member)
        return ResponseEntity.ok().body(WishItemResponseDTO)
    }

    @DeleteMapping
    fun deleteCartItem(
            @RequestBody wishItemRequestDTO: WishItemRequestDTO,
            @LoginMember member: MemberDTO,
            ): ResponseEntity<Unit> {
        wishItemService.delete(wishItemRequestDTO, member.id!!)
        return ResponseEntity.noContent().build()
    }
}