package ecommerce.repository

import ecommerce.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderRepositoryJpa : JpaRepository<Order, Long> {
    fun findByMemberId(memberId: Long): List<Order>

    @Query(
        """
        SELECT o FROM Order o
        JOIN FETCH o.items i
        JOIN FETCH i.productOption po
        JOIN FETCH po.product p
        WHERE o.id = :id
    """,
    )
    fun findByIdWithDetails(
        @Param("id") id: Long,
    ): Order?
}
