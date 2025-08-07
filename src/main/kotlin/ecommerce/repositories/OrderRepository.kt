package ecommerce.repositories

import ecommerce.entities.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByMemberId(memberId: Long): List<Order>
}
