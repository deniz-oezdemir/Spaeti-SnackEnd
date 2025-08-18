package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.CartCheckoutRequest
import ecommerce.dto.LoggedInMember
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.service.OrderService
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
    private val memberRepository: MemberRepositoryJpa,
) {
    @PostMapping
    fun placeOrder(
        @LoginMember member: LoggedInMember,
        @Valid @RequestBody req: PlaceOrderRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        val member =
            memberRepository.findById(member.id)

        if (member.isEmpty) {
            return ResponseEntity.badRequest().build()
        } else {
            val res = orderService.place(member.get(), req)
            return ResponseEntity
                .created(URI.create("/orders/${res.orderId}"))
                .body(res)
        }
    }

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
}
