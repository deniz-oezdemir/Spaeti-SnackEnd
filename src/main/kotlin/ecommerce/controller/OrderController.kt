package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.mappers.toDTO
import ecommerce.model.MemberDTO
import ecommerce.model.OrderResponseDTO
import ecommerce.model.PaymentRequestDTO
import ecommerce.services.OrderService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun createOrder(
        @Valid @RequestBody paymentRequestDTO: PaymentRequestDTO,
        @LoginMember member: MemberDTO,
    ): ResponseEntity<OrderResponseDTO> {
        val savedOrder = orderService.placeOrder(paymentRequestDTO, member)
        return ResponseEntity.ok(savedOrder.toDTO())
    }

    @GetMapping
    fun getUserOrders(
        @LoginMember member: MemberDTO,
    ): ResponseEntity<List<OrderResponseDTO>> {
        val orders = orderService.findOrdersByMemberId(member.id!!)
        return ResponseEntity.ok(orders)
    }
}
