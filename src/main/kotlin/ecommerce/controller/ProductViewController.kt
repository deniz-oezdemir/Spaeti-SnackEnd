package ecommerce.controller

import ecommerce.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping
class ProductViewController(private val productService: ProductService) {
    @GetMapping
    fun showProducts(model: Model): String {
        model.addAttribute("products", productService.findAll())
        return "product-list"
    }
}
