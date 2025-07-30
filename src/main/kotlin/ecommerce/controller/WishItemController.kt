package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.entities.Member
import ecommerce.model.WishItemResponseDTO
import ecommerce.model.MemberDTO
import ecommerce.model.ProductDTO
import ecommerce.model.WishItemRequestDTO
import ecommerce.services.WishItemService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wish")
class WishItemController(private val wishItemService: WishItemService) {
    @GetMapping
    fun getAllByMember(
        @LoginMember member: MemberDTO,
        @PageableDefault(size = 10, direction = Sort.Direction.ASC)
        page: Pageable
    ): Page<WishItemResponseDTO> {
//        val pageImpl = PageImpl(member.wishItems.toMutableList(), page, member.wishItems.size.toLong())
//        return pageImpl
        return wishItemService.findByMember(member.id!!, page = page)
    }

    @PostMapping
    fun saveWishItem(
        @RequestBody wishItemRequestDTO: WishItemRequestDTO,
        @LoginMember member: MemberDTO,
    ): ResponseEntity<WishItemResponseDTO> {
        val WishItemResponseDTO = wishItemService.save(wishItemRequestDTO, member)
        return ResponseEntity.ok().body(WishItemResponseDTO)
    }

    @DeleteMapping
    fun deleteWishItem(
        @RequestBody wishItemRequestDTO: WishItemRequestDTO,
        @LoginMember member: MemberDTO,
    ): ResponseEntity<Unit> {
        wishItemService.delete(wishItemRequestDTO, member.id!!)
        return ResponseEntity.noContent().build()
    }
}