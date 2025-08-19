package ecommerce.service

import ecommerce.dto.ProductRequest
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.repository.ProductRepositoryJpa
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepositoryJpa: ProductRepositoryJpa,
) {
    fun createProduct(productRequest: ProductRequest): Product {
        if (productRepositoryJpa.existsByName(productRequest.name)) {
            throw IllegalArgumentException("Product with name '${productRequest.name}' already exists.")
        }
        val product =
            Product(
                name = productRequest.name,
                price = productRequest.price,
                imageUrl = productRequest.imageUrl,
            )
        product.options.addAll(
            productRequest.options.map { dto ->
                Option(product = product, name = dto.name, quantity = dto.quantity)
            },
        )

        return productRepositoryJpa.save(product)
    }

    fun getAllProducts(
        page: Int,
        size: Int,
        sortBy: String = "name",
        direction: Sort.Direction = Sort.Direction.ASC,
    ): Page<Product> {
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        return productRepositoryJpa.findAll(pageable)
    }

    fun getAllProductsUnpaged(): List<Product> {
        return productRepositoryJpa.findAll()
    }

    fun getProductsByPrice(
        price: Double,
        page: Int,
        size: Int,
    ): Page<Product> {
        val pageable = PageRequest.of(page, size)
        return productRepositoryJpa.findAllByPrice(price, pageable)
    }

    fun updateProduct(
        id: Long,
        productRequest: ProductRequest,
    ) {
        val existingProduct =
            productRepositoryJpa.findById(id)
                .orElseThrow { IllegalArgumentException("Product with id $id not found.") }

        existingProduct.name = productRequest.name
        existingProduct.price = productRequest.price
        existingProduct.imageUrl = productRequest.imageUrl

        productRepositoryJpa.save(existingProduct)
    }

    fun deleteProduct(id: Long) {
        if (!productRepositoryJpa.existsById(id)) {
            throw IllegalArgumentException("Product with id $id does not exist.")
        }
        productRepositoryJpa.deleteById(id)
    }
}
