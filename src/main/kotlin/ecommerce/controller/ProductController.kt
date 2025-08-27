package ecommerce.controller

import ecommerce.entity.Product
import ecommerce.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Products", description = "APIs for product management")
@Validated
@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(summary = "Get all products (unpaged)")
    @GetMapping
    @ResponseBody
    fun readAll(): List<Product> {
        return productService.getAllProductsUnpaged()
    }

    @Operation(
        summary = "Get all products (paginated)",
        description = "Provides a paginated list of products. Supports sorting by fields like 'name' and 'price'.",
    )
    @GetMapping("/paged")
    fun getAllProducts(
        @PageableDefault(size = 10, sort = ["name"]) pageable: Pageable,
    ): Page<Product> {
        return productService.getAllProducts(
            page = pageable.pageNumber,
            size = pageable.pageSize,
            sortBy = pageable.sort.firstOrNull()?.property ?: "name",
            direction = pageable.sort.firstOrNull()?.direction ?: Sort.Direction.ASC,
        )
    }

    @Operation(summary = "Get products by price (paginated)")
    @GetMapping("/price")
    fun getByPrice(
        @RequestParam price: Double,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<Product> {
        return productService.getProductsByPrice(price, page, size)
    }
}
