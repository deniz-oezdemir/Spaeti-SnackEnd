package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.CartRequest
import ecommerce.dto.LoggedInMember
import ecommerce.dto.MemberResponse
import ecommerce.entity.Cart
import ecommerce.entity.CartItem
import ecommerce.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Cart", description = "APIs for managing the user shopping cart")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/protected/cart")
class CartController(
    private val cartService: CartService,
) {
    @Operation(
        summary = "Add an item to the cart",
        description = "Adds a product option to the current user's cart. If the item already exists, its quantity is increased.",
    )
    @ApiResponse(responseCode = "201", description = "Item added successfully")
    @PostMapping("/created")
    fun addToCart(
        @RequestBody request: CartRequest,
        @LoginMember member: LoggedInMember,
    ): ResponseEntity<Void> {
        when {
            request.hasId() ->
                cartService.addToCart(member.id, request.productOptionId!!, request.quantity)

            request.hasNames() ->
                cartService.addToCartByNames(member.id, request.productName!!, request.optionName!!, request.quantity)

            else -> throw IllegalArgumentException(
                "Provide either productOptionId or (productName + optionName)",
            )
        }
        return ResponseEntity.created(
            URI.create("/created"),
        ).build()
    }

    @Operation(summary = "Remove an item from the cart")
    @ApiResponse(responseCode = "204", description = "Item removed successfully")
    @DeleteMapping("/{cartItemId}")
    fun removeFromCart(
        @PathVariable cartItemId: Long,
    ): ResponseEntity<Void> {
        cartService.removeFromCart(cartItemId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Get the current user's cart")
    @GetMapping
    fun getCart(
        @LoginMember member: LoggedInMember,
    ): Cart {
        return cartService.getCart(member.id)
    }

    @Operation(summary = "Get items in the cart (paginated)", hidden = true)
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

    @Operation(summary = "Find cart items by quantity (paginated)", hidden = true)
    @GetMapping("/quantity")
    fun getByQuantity(
        @RequestParam quantity: Long,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<CartItem> {
        return cartService.getItemsByQuantity(quantity, page, size)
    }
}
