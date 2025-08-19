package ecommerce.repository

import ecommerce.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepositoryJpa : JpaRepository<Order, Long> {
    fun findByMemberId(memberId: Long): List<Order>
}
