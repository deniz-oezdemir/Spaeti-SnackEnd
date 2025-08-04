package ecommerce.controller

import ecommerce.annotation.CheckAdminOnly
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.OptionDTO
import ecommerce.model.TopProductDTO
import ecommerce.services.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @PostMapping("/options")
    fun createOption(
        @RequestBody optionDTO: OptionDTO,
    ): ResponseEntity<Unit> {
        adminService.createOption(optionDTO)
        return ResponseEntity.ok().build()
    }
}
