package ecommerce.repositories

import ecommerce.entities.CartItem
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByMemberId(memberId: Long): List<CartItem>

    fun existsByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    ): Boolean

    fun findByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    ): CartItem?

    fun deleteByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    )

    @Query(
        nativeQuery = true,
        value = """
        SELECT p.name AS name,
               COUNT(*) AS count,
               MAX(c.added_at) AS mostRecentAddedAt
        FROM cart_item c
        JOIN product p ON c.product_id = p.id
        WHERE c.added_at >=  DATEADD('DAY', -30, CURRENT_TIMESTAMP)
        GROUP BY c.product_id, p.name
        ORDER BY count DESC, mostRecentAddedAt DESC
        LIMIT 5
    """,
    )
    fun findTop5ProductsAddedInLast30Days(): List<TopProductDTO>

    @Query(
        nativeQuery = true,
        value = """
        SELECT DISTINCT m.id, m.name, m.email
        FROM cart_item c
        JOIN member m ON c.member_id = m.id
        WHERE c.added_at >= DATEADD('DAY', -7, CURRENT_TIMESTAMP)
    """,
    )
    fun findDistinctMembersWithCartActivityInLast7Days(): List<ActiveMemberDTO>
}
