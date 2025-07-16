package ecommerce.controller

import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI
import java.util.concurrent.atomic.AtomicLong

@Controller
@RequestMapping("/api/products")
class ProductController {
    private val index = AtomicLong(3)
    private val products: MutableMap<Long, Product> = hashMapOf()

    init {
        preloadProducts()
    }

    @GetMapping("")
    @ResponseBody
    fun getProducts(): List<Product> {
        return products.values.toList()
    }

    @PostMapping("")
    fun createProduct(
        @RequestBody product: Product,
    ): ResponseEntity<Product> {
        val id = index.getAndIncrement()
        val newProduct: Product = Product.toEntity(id, product)
        products[id] = newProduct
        val location = URI.create("/api/products/${newProduct.id}")
        return ResponseEntity.created(location).body(newProduct)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @RequestBody newProduct: Product,
        @PathVariable id: Long,
    ): ResponseEntity<Product> {
        val product = products[id] ?: throw NotFoundException("Product with Id: $id. Not found.")
        product.update(newProduct)
        return ResponseEntity.ok().body(product)
    }

    @PatchMapping("/{id}")
    fun patchProduct(
        @RequestBody newProduct: Product,
        @PathVariable id: Long,
    ): ResponseEntity<Product> {
        val product = products[id] ?: throw NotFoundException("Product with Id: $id. Not found.")
        product.update(newProduct)
        return ResponseEntity.ok().body(product)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val removed = products.remove(id)
        return if (removed == null) {
            throw NotFoundException("Product with Id: $id. Not found.")
        } else {
            ResponseEntity.ok().build()
        }
    }

    private fun preloadProducts() {
        listOf(
            Product(1L, "Car", 1.0, "www.some"),
            Product(2L, "Bike", 0.5, "www.example.com/bike"),
            Product(3L, "Truck", 2.0, "www.example.com/truck"),
        ).forEach { product ->
            products[product.id!!] = product
        }
        index.set(products.keys.maxOrNull()?.plus(1) ?: 1)
    }
}
