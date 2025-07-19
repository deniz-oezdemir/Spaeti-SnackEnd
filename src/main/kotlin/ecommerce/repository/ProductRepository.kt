package ecommerce.repository

import ecommerce.model.Product

interface ProductRepository {
    fun findAll(): List<Product>

    fun findById(id: Long): Product?

    fun save(product: Product): Product

    fun update(
        id: Long,
        product: Product,
    ): Product

    fun delete(id: Long)

    fun deleteAll()

    fun patch(
        id: Long,
        product: Product,
    ): Product
}
