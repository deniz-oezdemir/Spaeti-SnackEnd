package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.CartRequest
import ecommerce.dto.LoggedInMember
import ecommerce.dto.MemberResponse
import ecommerce.entity.Cart
import ecommerce.entity.CartItem
import ecommerce.service.CartService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/protected/cart")
class CartController(
    private val cartService: CartService,
) {
    @PostMapping("/created")
    fun addToCart(
        @RequestBody request: CartRequest,
        @LoginMember member: LoggedInMember,
    ): ResponseEntity<Void> {
        cartService.addToCart(member.id, request.productOptionId, 1)
        return ResponseEntity.created(
            URI.create("/created"),
        ).build()
    }

    @DeleteMapping("/{cartItemId}")
    fun removeFromCart(
        @PathVariable cartItemId: Long,
    ): ResponseEntity<Void> {
        cartService.removeFromCart(cartItemId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getCart(
        @LoginMember member: LoggedInMember,
    ): Cart {
        return cartService.getCart(member.id)
    }

    @GetMapping("/paged")
    fun getCartItems(
        @PageableDefault(size = 10, sort = ["created_at"]) pageable: Pageable,
        @LoginMember member: MemberResponse,
    ): Page<CartItem> {
        return cartService.getCartItems(
            memberId = member.id,
            page = pageable.pageNumber,
            size = pageable.pageSize,
            sortBy = pageable.sort.firstOrNull()?.property ?: "created_at",
            direction = pageable.sort.firstOrNull()?.direction ?: Sort.Direction.ASC,
        )
    }

    @GetMapping("/quantity")
    fun getByQuantity(
        @RequestParam quantity: Long,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<CartItem> {
        return cartService.getItemsByQuantity(quantity, page, size)
    }
}
