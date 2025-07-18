package ecommerce.controller

import ecommerce.repository.ProductRepositoryImpl
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping
class ProductViewController(private val productRepositoryImpl: ProductRepositoryImpl) {
    @GetMapping
    fun showProducts(model: Model): String {
        model.addAttribute("products", productRepositoryImpl.findAll())
        return "product-list"
    }
}
