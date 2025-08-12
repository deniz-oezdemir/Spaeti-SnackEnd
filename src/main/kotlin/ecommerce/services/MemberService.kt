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
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class MemberService(private val memberRepository: MemberRepository) {
    @Transactional(readOnly = true)
    fun findAll(): List<MemberDTO> {
        return memberRepository.findAll().map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): MemberDTO =
        memberRepository.findByIdOrNull(id)?.toDto()
            ?: throw EmptyResultDataAccessException("Member with ID $id not found", 1)

    @Transactional(readOnly = true)
    fun findByEmail(email: String): MemberDTO {
        return memberRepository.findByEmail(email)?.toDto()
            ?: throw EmptyResultDataAccessException("Member with Email $email not found", 1)
    }

    @Transactional
    fun save(memberDTO: MemberDTO): MemberDTO {
        validateEmailUniqueness(memberDTO.email)
        val saved =
            memberRepository.save(memberDTO.toEntity())
                ?: throw OperationFailedException("Failed to save product")
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun validateEmailUniqueness(email: String) {
        if (memberRepository.existsByEmail(email)) {
            throw OperationFailedException("Member with email '$email' already exists")
        }
    }

    @Transactional
    fun deleteAll() {
        memberRepository.deleteAll()
    }
}
