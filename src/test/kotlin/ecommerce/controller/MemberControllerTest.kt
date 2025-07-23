package ecommerce.controller

import ecommerce.model.MemberDTO
import ecommerce.model.TokenRequestDTO
import ecommerce.model.TokenResponseDTO
import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberControllerTest {

    @Test
    fun createMember() {
        val accessToken =
            RestAssured
                .given().log().all()
                .body(TokenRequestDTO(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/register")
                .then().log().all().extract().`as`(TokenResponseDTO::class.java).accessToken

        assertThat(accessToken).isNotNull()

        val member =
            RestAssured
                .given().log().all()
                .header("Authorization", "Bearer $accessToken")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().get("/api/members/me/token")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract().`as`(MemberDTO::class.java)

        assertThat(member.email).isEqualTo(EMAIL)
    }
    companion object {
        private const val EMAIL = "email@email.com"
        private const val PASSWORD = "1234"
    }
}