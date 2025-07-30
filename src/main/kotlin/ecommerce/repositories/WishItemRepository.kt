package ecommerce.repositories

import ecommerce.entities.WishItem
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface WishItemRepository : JpaRepository<WishItem, Long> {
    @Transactional
    fun findByMemberId(memberId: Long): List<WishItem>

    @Transactional
    fun findByMemberId(memberId: Long, page: Pageable): Page<WishItem>

    @Transactional
    fun findByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    ): WishItem?

    @Transactional
    fun deleteByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    )
}