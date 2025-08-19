package ecommerce.repository

import ecommerce.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepositoryJpa : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: Long): Payment?
}
