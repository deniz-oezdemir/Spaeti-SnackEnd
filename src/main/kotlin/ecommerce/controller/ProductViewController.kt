package ecommerce.controller

import ecommerce.annotation.IgnoreCheckLogin
import ecommerce.model.ProductRequestDTO
import ecommerce.model.ProductResponseDTO
import ecommerce.services.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping
class ProductViewController(private val productService: ProductService) {
    @IgnoreCheckLogin
    @GetMapping
    fun showProducts(
        model: Model,
        @PageableDefault(size = 10, sort = ["name"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): String {
        val products = productService.findAll(pageable)

        model.addAttribute("products", products.content)
        model.addAttribute("currentPage", products.number)
        model.addAttribute("totalPages", products.totalPages)
        model.addAttribute("productDTO", ProductResponseDTO.empty())
        model.addAttribute("hasErrors", false)
        return "product-list"
    }

    @PostMapping("/products")
    fun createProduct(
        @Valid productDTO: ProductRequestDTO,
        bindingResult: BindingResult,
        model: Model,
        @PageableDefault(size = 10, sort = ["name"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): String {
        if (bindingResult.hasErrors()) {
            val products = productService.findAll(pageable)
            model.addAttribute("products", products.content)
            model.addAttribute("currentPage", products.number)
            model.addAttribute("totalPages", products.totalPages)
            model.addAttribute("productDTO", productDTO)
            model.addAttribute("hasErrors", bindingResult.hasErrors())
            return "product-list"
        }
        productService.save(productDTO)
        return "redirect:/"
    }
}
