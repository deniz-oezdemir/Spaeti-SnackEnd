package ecommerce.repositories

import ecommerce.entities.Product
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun existsByName(name: String): Boolean
}
