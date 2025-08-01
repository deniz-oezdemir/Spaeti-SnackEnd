package ecommerce.repositories

import ecommerce.entities.WishItem
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface WishItemRepository : JpaRepository<WishItem, Long> {
    fun findByMemberId(memberId: Long): List<WishItem>

    fun findByMemberId(
        memberId: Long,
        page: Pageable,
    ): Page<WishItem>

    fun findByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    ): WishItem?

    fun deleteByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    )
}
