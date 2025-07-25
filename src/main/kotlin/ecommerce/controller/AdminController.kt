package ecommerce.controller

import ecommerce.annotation.CheckAdminOnly
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO
import ecommerce.services.AdminService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@CheckAdminOnly
class AdminController(private val adminService: AdminService) {
    @GetMapping("/top-products")
    fun getTopProducts(): List<TopProductDTO> = adminService.findTopProductsAddedInList30Days()

    @GetMapping("/active-members")
    fun getActiveMembers(): List<ActiveMemberDTO> = adminService.findMembersWithRecentCartActivity()
}
