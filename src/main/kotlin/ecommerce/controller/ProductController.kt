package ecommerce.controller

import ecommerce.annotation.CheckAdminOnly
import ecommerce.annotation.IgnoreCheckLogin
import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.services.ProductService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ProductController(private val productService: ProductService) {
    @IgnoreCheckLogin
    @GetMapping(PRODUCT_PATH)
    fun getProducts(): List<ProductDTO> = productService.findAll()

    @IgnoreCheckLogin
    @GetMapping(PRODUCT_PATH_ID)
    fun getProductById(
        @PathVariable id: Long,
    ): ResponseEntity<ProductDTO> = ResponseEntity.ok(productService.findById(id))

    @CheckAdminOnly
    @PostMapping(PRODUCT_PATH)
    fun createProduct(
        @Valid @RequestBody productDTO: ProductDTO,
    ): ResponseEntity<ProductDTO> {
        val saved = productService.save(productDTO)
        return ResponseEntity.created(URI.create("$PRODUCT_PATH/${saved.id}")).body(saved)
    }

    @CheckAdminOnly
    @PutMapping(PRODUCT_PATH_ID)
    fun updateProductById(
        @Valid @RequestBody productDTO: ProductDTO,
        @PathVariable id: Long,
    ): ResponseEntity<ProductDTO> = ResponseEntity.ok(productService.updateById(id, productDTO))

    @CheckAdminOnly
    @PatchMapping(PRODUCT_PATH_ID)
    fun patchProductById(
        @Valid @RequestBody productPatchDTO: ProductPatchDTO,
        @PathVariable id: Long,
    ): ResponseEntity<ProductDTO> = ResponseEntity.ok(productService.patchById(id, productPatchDTO))

    @CheckAdminOnly
    @DeleteMapping(PRODUCT_PATH_ID)
    fun deleteProductById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @CheckAdminOnly
    @DeleteMapping(PRODUCT_PATH)
    fun deleteAllProducts(): ResponseEntity<String> {
        productService.deleteAll()
        return ResponseEntity.noContent().build()
    }

    companion object {
        const val PRODUCT_PATH = "/api/products"
        const val PRODUCT_PATH_ID = "$PRODUCT_PATH/{id}"
    }
}
