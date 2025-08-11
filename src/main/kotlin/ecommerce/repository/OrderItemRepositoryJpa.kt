package ecommerce.repository

import ecommerce.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepositoryJpa : JpaRepository<OrderItem, Long>
