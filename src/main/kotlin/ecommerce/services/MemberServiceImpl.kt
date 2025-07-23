package ecommerce.services

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDto
import ecommerce.mappers.toEntity
import ecommerce.model.MemberDTO
import ecommerce.repositories.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberServiceImpl(private val memberRepository: MemberRepository) : MemberService {
    override fun findAll(): List<MemberDTO> {
        return memberRepository.findAll().map { it.toDto() }
    }

    override fun findById(id: Long): MemberDTO =
        memberRepository.findById(id)?.toDto() ?: throw NotFoundException("Member with ID $id not found")

    override fun findByEmail(email: String): MemberDTO {
        return memberRepository.findByEmail(email)?.toDto()
            ?: throw NotFoundException("Member with Email $email not found")
    }

    override fun enrichedWithRole(memberDTO: MemberDTO): MemberDTO {
        val member =
            memberRepository.findByEmail(memberDTO.email)
                ?: throw NotFoundException("Member with Email ${memberDTO.email} not found")
        return member.toDto().copy(role = member.role)
    }

    override fun save(memberDTO: MemberDTO): MemberDTO {
        validateEmailUniqueness(memberDTO.email)
        val saved =
            memberRepository.save(memberDTO.toEntity())
                ?: throw OperationFailedException("Failed to save product")
        return saved.toDto()
    }

    override fun updateById(
        id: Long,
        memberDTO: MemberDTO,
    ): MemberDTO? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun validateEmailUniqueness(email: String) {
        if (memberRepository.existsByEmail(email)) {
            throw OperationFailedException("Member with email '$email' already exists")
        }
    }
}
