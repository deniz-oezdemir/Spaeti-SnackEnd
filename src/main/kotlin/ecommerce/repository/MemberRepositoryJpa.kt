package ecommerce.repository
import ecommerce.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepositoryJpa : JpaRepository<Member, Long> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): Member?
}
