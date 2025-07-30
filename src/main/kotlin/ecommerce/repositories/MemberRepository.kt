package ecommerce.repositories

import ecommerce.entities.Member
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    @Transactional
    fun findByEmail(email: String): Member?

    @Transactional
    fun save(member: Member): Member?

    @Transactional
    fun existsByEmail(email: String): Boolean
}
