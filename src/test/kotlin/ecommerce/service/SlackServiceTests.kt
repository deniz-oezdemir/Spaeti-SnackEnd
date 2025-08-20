package ecommerce.service

import ecommerce.entity.Member
import ecommerce.repository.MemberRepositoryJpa
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SlackServiceTests {
    @Autowired
    private lateinit var memberRepositoryJpa: MemberRepositoryJpa

    @Autowired
    private lateinit var slackService: SlackService

    @BeforeEach
    fun setupTestData() {
        memberRepositoryJpa.deleteAll()

        memberRepositoryJpa.save(
            Member(
                name = "Test User",
                email = "test@example.com",
                password = "testpassword",
                role = "USER",
                slackUserId = "U123456",
            ),
        )
    }

    @Test
    fun `should send slack message`() {
        val member =
            memberRepositoryJpa.findById(1).orElseThrow {
                AssertionError("Member with ID 1 not found")
            }
        assertDoesNotThrow {
            member.slackUserId?.let { slackUserId ->
                slackService.sendMessage(slackUserId, "Test from JUnit")
            }
        }
    }
}
