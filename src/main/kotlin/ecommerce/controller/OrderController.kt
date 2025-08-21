package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.CartCheckoutRequest
import ecommerce.dto.GiftCheckoutRequest
import ecommerce.dto.LoggedInMember
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.service.MemberService
import ecommerce.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
    private val memberService: MemberService,
    private val memberRepository: MemberRepositoryJpa,
) {
    @Operation(summary = "Place an order for a single item")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Order placed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
            ApiResponse(responseCode = "500", description = "Payment processing failed")
        ]
    )
    @PostMapping    fun placeOrder(
        @LoginMember principal: LoggedInMember,
        @Valid @RequestBody req: PlaceOrderRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        val member = memberService.getByIdOrThrow(principal.id)
        val res = orderService.place(member, req)
        return ResponseEntity.created(URI.create("/orders/${res.orderId}")).body(res)
    }

    @Operation(summary = "Create an order from all items in the cart")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Checkout successful"),
            ApiResponse(responseCode = "400", description = "Invalid input or empty cart"),
            ApiResponse(responseCode = "500", description = "Payment processing failed")
        ]
    )
    @PostMapping("/checkout")
    fun checkoutCart(
        @LoginMember member: LoggedInMember,
        @Valid @RequestBody req: CartCheckoutRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        val memberEntity =
            memberRepository.findById(member.id)
                .orElseThrow { NoSuchElementException("Member not found for ID: ${member.id}") }

        val res = orderService.checkoutCart(memberEntity, req)
        return ResponseEntity
            .created(URI.create("/orders/${res.orderId}"))
            .body(res)
    }

    @Operation(summary = "Create a gift order from all items in the cart")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Gift order placed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input or empty cart"),
            ApiResponse(responseCode = "500", description = "Payment processing failed")
        ]
    )
    @PostMapping("/gift")
    fun placeGiftOrder(
        @LoginMember member: LoggedInMember,
        @Valid @RequestBody req: GiftCheckoutRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        val memberEntity = memberService.getByIdOrThrow(member.id) // <-- central lookup
        val res = orderService.placeGift(memberEntity, req)
        return ResponseEntity.created(URI.create("/orders/${res.orderId}")).body(res)
    }
}
