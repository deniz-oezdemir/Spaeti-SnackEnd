package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.model.MemberDTO
import ecommerce.model.PaymentRequestDTO
import ecommerce.services.PaymentService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    @PostMapping
    fun createPayment(
        @Valid @RequestBody paymentRequestDTO: PaymentRequestDTO,
        @LoginMember member: MemberDTO
    ): ResponseEntity<Unit> {
        paymentService.processPayment(paymentRequestDTO, member)
        return ResponseEntity.ok().build()
    }
}
