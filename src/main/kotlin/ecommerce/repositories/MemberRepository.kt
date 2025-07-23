package ecommerce.repositories

import ecommerce.entities.Member

interface MemberRepository {
    fun findAll(): List<Member>

    fun findById(id: Long): Member?

    fun findByEmail(email: String): Member?

    fun save(member: Member): Member?

    fun updateById(
        id: Long,
        member: Member,
    ): Member?

    fun deleteById(id: Long): Boolean

    fun existsByEmail(email: String): Boolean
}
