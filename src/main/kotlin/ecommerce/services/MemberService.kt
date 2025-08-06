package ecommerce.services

import ecommerce.model.MemberDTO

interface MemberService {
    fun findAll(): List<MemberDTO>

    fun findById(id: Long): MemberDTO

    fun findByEmail(email: String): MemberDTO

    fun save(memberDTO: MemberDTO): MemberDTO

    fun validateEmailUniqueness(email: String)

    fun deleteAll()
}
