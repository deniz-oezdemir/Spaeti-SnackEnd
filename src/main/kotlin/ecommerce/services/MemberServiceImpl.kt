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
class MemberServiceImpl(private val memberRepository: MemberRepository) : MemberService {
    @Transactional(readOnly = true)
    override fun findAll(): List<MemberDTO> {
        return memberRepository.findAll().map { it.toDto() }
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): MemberDTO =
        memberRepository.findByIdOrNull(id)?.toDto()
            ?: throw EmptyResultDataAccessException("Member with ID $id not found", 1)

    @Transactional(readOnly = true)
    override fun findByEmail(email: String): MemberDTO {
        return memberRepository.findByEmail(email)?.toDto()
            ?: throw EmptyResultDataAccessException("Member with Email $email not found", 1)
    }

    @Transactional
    override fun save(memberDTO: MemberDTO): MemberDTO {
        validateEmailUniqueness(memberDTO.email)
        val saved =
            memberRepository.save(memberDTO.toEntity())
                ?: throw OperationFailedException("Failed to save product")
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    override fun validateEmailUniqueness(email: String) {
        if (memberRepository.existsByEmail(email)) {
            throw OperationFailedException("Member with email '$email' already exists")
        }
    }

    @Transactional
    override fun deleteAll() {
        memberRepository.deleteAll()
    }
}
