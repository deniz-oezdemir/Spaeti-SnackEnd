package ecommerce.controller

import ecommerce.dto.ProductRequest
import ecommerce.entity.Product
import ecommerce.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Products", description = "Admin-only product management APIs")
@RestController
@RequestMapping("/api/protected/admin/products")
class AdminProductController(
    private val productService: ProductService,
) {
    @Operation(
        summary = "Create a new product (admin)",
        description = "Creates a new product with one or more options. Product name must be unique.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Product created"),
            ApiResponse(responseCode = "400", description = "Validation error / duplicate name"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (not admin)"),
        ],
    )
    @PostMapping("/create")
    fun create(
        @Valid @RequestBody req: ProductRequest,
    ): ResponseEntity<Void> {
        val product: Product = productService.createProduct(req)
        return ResponseEntity.created(java.net.URI.create("/products/${product.id}")).build()
    }

    @Operation(summary = "Update an existing product (admin)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product updated"),
            ApiResponse(responseCode = "400", description = "Validation error"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (not admin)"),
            ApiResponse(responseCode = "404", description = "Product not found"),
        ],
    )
    @PutMapping("/update/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: ProductRequest,
    ): ResponseEntity<Void> {
        productService.updateProduct(id, req)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Delete a product (admin)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product deleted"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (not admin)"),
            ApiResponse(responseCode = "404", description = "Product not found"),
        ],
    )
    @DeleteMapping("/delete/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }
}
