package ecommerce.controller

import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import ecommerce.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/protected/admin")
class AdminController(
    private val cartService: CartService,
) {
    @Operation(summary = "Get top 5 products added to carts in last 30 days")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved top products"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (user is not an admin)")
        ]
    )
    @GetMapping("/top-products")
    fun findTop5ProductsInLast30Days(): ResponseEntity<List<TopProductStatResponse>> {
        val topProducts = cartService.findTop5ProductsInLast30Days()
        return ResponseEntity.ok(topProducts)
    }

    @Operation(summary = "Get users with cart activity in last 7 days")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved active members"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (user is not an admin)")
        ]
    )
    @GetMapping("/cart-activity")    fun findMembersWithCartActivityInLast7Days(): ResponseEntity<List<MemberResponse>> {
        val activeMembers = cartService.findMembersWithCartActivityInLast7Days()
        return ResponseEntity.ok(activeMembers)
    }
}
