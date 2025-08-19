package ecommerce.controller

import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import ecommerce.service.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/protected/admin")
class AdminController(
    private val cartService: CartService,
) {
    @GetMapping("/top-products")
    fun findTop5ProductsInLast30Days(): ResponseEntity<List<TopProductStatResponse>> {
        val topProducts = cartService.findTop5ProductsInLast30Days()
        return ResponseEntity.ok(topProducts)
    }

    @GetMapping("/cart-activity")
    fun findMembersWithCartActivityInLast7Days(): ResponseEntity<List<MemberResponse>> {
        val activeMembers = cartService.findMembersWithCartActivityInLast7Days()
        return ResponseEntity.ok(activeMembers)
    }
}
