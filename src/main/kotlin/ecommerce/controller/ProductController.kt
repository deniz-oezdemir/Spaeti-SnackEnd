package ecommerce.controller

import ecommerce.dto.ProductRequest
import ecommerce.entity.Product
import ecommerce.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Tag(name = "Products", description = "APIs for product management")
@Validated
@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(
        summary = "Create a new product",
        description = "Creates a new product with one or more options. Product name must be unique.",
        responses = [
            ApiResponse(responseCode = "201", description = "Product created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input, such as a validation error or duplicate name"),
        ],
    )
    @PostMapping
    @ResponseBody
    fun create(
        @Valid
        @RequestBody productRequest: ProductRequest,
    ): ResponseEntity<Void> {
        val product = productService.createProduct(productRequest)
        return ResponseEntity.created(URI.create("/products/${product.id}")).build()
    }

    @Operation(summary = "Get all products (unpaged)")
    @GetMapping
    @ResponseBody
    fun readAll(): List<Product> {
        return productService.getAllProductsUnpaged()
    }

    @Operation(summary = "Update an existing product")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input data"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    @PutMapping("/{id}")
    @ResponseBody
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody @Valid productRequest: ProductRequest,
    ): ResponseEntity<Void> {
        productService.updateProduct(id, productRequest)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Delete a product")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseBody
    fun delete(
        @PathVariable("id") id: Long,
    ): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Get all products (paginated)",
        description = "Provides a paginated list of products. Supports sorting by fields like 'name' and 'price'."
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
