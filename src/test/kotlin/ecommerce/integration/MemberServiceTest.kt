package ecommerce.integration

import ecommerce.entities.Member
import ecommerce.exception.OperationFailedException
import ecommerce.model.MemberDTO
import ecommerce.repositories.MemberRepository
import ecommerce.services.MemberServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class MemberServiceTest {
    @Autowired
    private lateinit var memberService: MemberServiceImpl

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `findAll should return all members`() {
        memberRepository.save(Member(name = "a", email = "a@a.com", password = "123", role = Member.Role.CUSTOMER))
        memberRepository.save(Member(name = "b", email = "b@b.com", password = "456", role = Member.Role.ADMIN))

        val result = memberService.findAll()

        assertThat(result).hasSize(13)
        assertThat(result.map { it.email }).contains("a@a.com", "b@b.com")
    }

    @Test
    fun `findById should return matching member`() {
        val saved = memberRepository.save(Member(name = "c", email = "c@c.com", password = "pass", role = Member.Role.ADMIN))!!

        val found = memberService.findById(saved.id!!)

        assertThat(found.email).isEqualTo("c@c.com")
    }

    @Test
    fun `findById should throw if not found`() {
        val ex =
            assertThrows<EmptyResultDataAccessException> {
                memberService.findById(999L)
            }
        assertThat(ex.message).contains("Member with ID")
    }

    @Test
    fun `findByEmail should return matching member`() {
        memberRepository.save(Member(name = "find me", email = "findme@test.com", password = "pw", role = Member.Role.CUSTOMER))

        val found = memberService.findByEmail("findme@test.com")

        assertThat(found.email).isEqualTo("findme@test.com")
    }

    @Test
    fun `findByEmail should throw if not found`() {
        val ex =
            assertThrows<EmptyResultDataAccessException> {
                memberService.findByEmail("missing@test.com")
            }
        assertThat(ex.message).contains("Member with Email")
    }

    @Test
    fun `save should persist member and return DTO`() {
        val dto = MemberDTO(name = "new", email = "new@test.com", password = "secure")

        val saved = memberService.save(dto)

        assertThat(saved.id).isNotNull()
        assertThat(saved.email).isEqualTo("new@test.com")
    }

    @Test
    fun `save should throw if email exists`() {
        memberRepository.save(Member(name = "exists", email = "exists@test.com", password = "old", role = Member.Role.CUSTOMER))

        val ex =
            assertThrows<OperationFailedException> {
                memberService.save(MemberDTO(name = "exists2", email = "exists@test.com", password = "new"))
            }

        assertThat(ex.message).contains("already exists")
    }

    @Test
    fun `validateEmailUniqueness should pass if email doesn't exist`() {
        assertThat(memberService.validateEmailUniqueness("unique@test.com")).isEqualTo(Unit)
    }

    @Test
    fun `validateEmailUniqueness should throw if email exists`() {
        memberRepository.save(Member(name = "exists", email = "exists@test.com", password = "pw", role = Member.Role.CUSTOMER))

        assertThrows<OperationFailedException> {
            memberService.validateEmailUniqueness("exists@test.com")
        }
    }
}
