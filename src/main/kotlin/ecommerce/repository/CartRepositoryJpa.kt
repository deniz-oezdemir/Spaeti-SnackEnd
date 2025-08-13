package ecommerce.repository

import ecommerce.dto.MemberResponse
import ecommerce.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CartRepositoryJpa : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Cart?

    @Query(
        """
    SELECT DISTINCT m.id, m.name, m.email, m.role
    FROM members m
    JOIN carts c ON m.id = c.member_id
    WHERE c.created_at >= NOW() - INTERVAL 7 DAY
    """,
        nativeQuery = true,
    )
    fun findMembersWithCartActivityInLast7Days(): List<MemberResponse>
}
