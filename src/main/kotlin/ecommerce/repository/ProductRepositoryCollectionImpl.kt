package ecommerce.repository

import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicLong

@Repository
class ProductRepositoryCollectionImpl: ProductRepository {
        private val products = mutableMapOf<Long, Product>()
        private val index = AtomicLong(1)

        init {
            preloadProducts()
        }

        override fun findAll(): List<Product> = products.values.toList()

        override fun findById(id: Long): Product = products[id] ?: throw NotFoundException("Product with Id: $id. Not found.")

        override fun save(product: Product): Product {
            val id = index.getAndIncrement()
            val saved = Product.toEntity(id, product)
            products[id] = saved
            return saved
        }

        override fun update(
            id: Long,
            product: Product,
        ): Product {
            val existing = findById(id)
            val updated = existing.copyFrom(product)
            products[id] = updated
            return updated
        }

        override fun patch(
            id: Long,
            product: Product,
        ): Product {
            val existing = findById(id)
            val updated = existing.partialUpdate(product)
            products[id] = updated
            return updated
        }

        override fun delete(id: Long) {
            if (products.remove(id) == null) throw NotFoundException("Product with Id: $id. Not found.")
        }

        override fun deleteAll() {
            products.clear()
        }

        private fun preloadProducts() {
            listOf(
                Product(index.getAndIncrement(), "Car", 1000.0, "www.some.com"),
                Product(index.getAndIncrement(), "Bike", 200.0, "www.example.com/bike"),
                Product(index.getAndIncrement(), "Truck", 30000.0, "www.example.com/truck"),
            ).forEach { products[it.id!!] = it }
        }
    }
