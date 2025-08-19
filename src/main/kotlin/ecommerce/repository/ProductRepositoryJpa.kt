package ecommerce.repository

import ecommerce.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepositoryJpa : JpaRepository<Product, Long> {
    fun findByName(name: String): Product?

    fun existsByName(name: String): Boolean

    fun findAllByPrice(
        price: Double,
        pageable: Pageable,
    ): Page<Product>

    override fun findAll(pageable: Pageable): Page<Product>
}
