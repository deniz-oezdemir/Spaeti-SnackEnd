package ecommerce.repository

import ecommerce.dto.TopProductStatResponse
import ecommerce.entity.CartItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CartItemRepositoryJpa : JpaRepository<CartItem, Long> {
    fun findByCartIdAndProductOptionId(
        cartId: Long,
        productId: Long,
    ): CartItem?

    fun findAllByQuantity(
        quantity: Long,
        pageable: Pageable,
    ): Page<CartItem>

    override fun findAll(pageable: Pageable): Page<CartItem>

    fun findAllByCartId(
        cartId: Long,
        pageable: Pageable,
    ): Page<CartItem>

    @Query(
        value = """
               SELECT 
                p.name AS productName,
                COUNT(*) AS timesAdded,
                MAX(ci.created_at) AS mostRecentAddedTime
            FROM cart_item ci
            JOIN product_option po ON ci.product_option_id = po.id
            JOIN product p ON po.product_id = p.id
            WHERE ci.created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
            GROUP BY p.name
            ORDER BY timesAdded DESC, mostRecentAddedTime DESC
            LIMIT 5
        """,
        nativeQuery = true,
    )
    fun findTop5ProductsInLast30Days(): List<TopProductStatResponse>
}
