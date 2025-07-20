package ecommerce.controller

import ecommerce.model.ProductDTO
import ecommerce.services.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping
class ProductViewController(private val productService: ProductService) {
    @GetMapping
    fun showProducts(
        model: Model,
        @RequestParam(required = false) pageNumber: Int = 1,
        @RequestParam(required = false) pageSize: Int = 10
    ): String {
        val (products, totalCount) = productService.findAllPaginated(pageNumber, pageSize)
        val totalPages = (totalCount + pageSize - 1) / pageSize

        model.addAttribute("products", products)
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("totalPages", totalPages)
        return "product-list"
    }

    @PostMapping("/products")
    fun createProduct(productDTO: ProductDTO): String {
        productService.save(productDTO)
        return "redirect:/"
    }
}
