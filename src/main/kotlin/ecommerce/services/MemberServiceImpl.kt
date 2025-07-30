package ecommerce.services

import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDto
import ecommerce.mappers.toEntity
import ecommerce.model.MemberDTO
import ecommerce.repositories.MemberRepository
import org.springframework.context.annotation.Primary
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@Primary
class MemberServiceImpl(private val memberRepository: MemberRepository) : MemberService {
    override fun findAll(): List<MemberDTO> {
        return memberRepository.findAll().map { it.toDto() }
    }

    override fun findById(id: Long): MemberDTO =
        memberRepository.findByIdOrNull(id)?.toDto()
            ?: throw EmptyResultDataAccessException("Member with ID $id not found", 1)

    override fun findByEmail(email: String): MemberDTO {
        return memberRepository.findByEmail(email)?.toDto()
            ?: throw EmptyResultDataAccessException("Member with Email $email not found", 1)
    }

    override fun save(memberDTO: MemberDTO): MemberDTO {
        validateEmailUniqueness(memberDTO.email)
        val saved =
            memberRepository.save(memberDTO.toEntity())
                ?: throw OperationFailedException("Failed to save product")
        return saved.toDto()
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
