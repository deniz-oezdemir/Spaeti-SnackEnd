package ecommerce.service

import ecommerce.entity.Member
import ecommerce.repository.MemberRepositoryJpa
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepositoryJpa,
) {
    fun getByIdOrThrow(id: Long): Member {
        val member =
            memberRepository.findById(id).orElse(null)
                ?: throw NoSuchElementException("Member not found")
        return member
    }
}
