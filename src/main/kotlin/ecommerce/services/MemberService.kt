package ecommerce.services

import ecommerce.model.MemberDTO

interface MemberService {
    fun findAll(): List<MemberDTO>

    fun findById(id: Long): MemberDTO

    fun findByEmail(email: String): MemberDTO

    fun enrichedWithRole(memberDTO: MemberDTO): MemberDTO

    fun save(memberDTO: MemberDTO): MemberDTO

    fun updateById(
        id: Long,
        memberDTO: MemberDTO,
    ): MemberDTO?

    fun deleteById(id: Long)

    fun validateEmailUniqueness(email: String)
}
