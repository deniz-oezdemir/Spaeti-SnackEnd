package ecommerce.repositories

import ecommerce.entities.Product

interface ProductRepository {
    fun findAll(): List<Product>

    fun findAllPaginated(
        offset: Int,
        size: Int,
    ): List<Product>

    fun findById(id: Long): Product?

    fun save(product: Product): Product?

    fun updateById(
        id: Long,
        product: Product,
    ): Product?

    fun deleteById(id: Long): Boolean

    fun deleteAll(): Boolean

    fun countAll(): Int

    fun existsByName(name: String): Boolean
}
