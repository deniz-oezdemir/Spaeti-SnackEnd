package ecommerce.repositories

import ecommerce.entities.Member
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?

    fun save(member: Member): Member?

    fun existsByEmail(email: String): Boolean
}
