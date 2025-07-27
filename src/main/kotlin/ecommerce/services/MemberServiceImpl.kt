package ecommerce.services

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toEntity
import ecommerce.mappers.toResponseDto
import ecommerce.model.MemberDTO
import ecommerce.repositories.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberServiceImpl(private val memberRepository: MemberRepository) : MemberService {
    override fun findAll(): List<MemberDTO> {
        return memberRepository.findAll().map { it.toResponseDto() }
    }

    override fun findById(id: Long): MemberDTO =
        memberRepository.findById(id)?.toResponseDto() ?: throw NotFoundException("Member with ID $id not found")

    override fun findByEmail(email: String): MemberDTO {
        return memberRepository.findByEmail(email)?.toResponseDto()
            ?: throw NotFoundException("Member with Email $email not found")
    }

    override fun save(memberDTO: MemberDTO): MemberDTO {
        validateEmailUniqueness(memberDTO.email)
        val saved =
            memberRepository.save(memberDTO.toEntity())
                ?: throw OperationFailedException("Failed to save product")
        return saved.toResponseDto()
    }

    override fun validateEmailUniqueness(email: String) {
        if (memberRepository.existsByEmail(email)) {
            throw OperationFailedException("Member with email '$email' already exists")
        }
    }

    override fun deleteAll() {
        memberRepository.deleteAll()
    }
}
