package ecommerce.repositories

import ecommerce.entities.Member
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class MemberRepositoryImpl(private val jdbc: JdbcTemplate) : MemberRepository {
    private val memberRowMapper =
        RowMapper<Member> { rs: ResultSet, _: Int ->
            Member(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password"),
                Member.Role.valueOf(rs.getString("role")),
            )
        }

    private val insert: SimpleJdbcInsert by lazy {
        SimpleJdbcInsert(jdbc)
            .withTableName("member")
            .usingGeneratedKeyColumns("id")
    }

    override fun findAll(): List<Member> {
        val sql = "SELECT * FROM member"
        return jdbc.query(sql, memberRowMapper)
    }

    override fun findById(id: Long): Member? {
        val sql = "SELECT * FROM member WHERE id = ?"
        return jdbc.queryForObject(sql, memberRowMapper, id)
    }

    override fun findByEmail(email: String): Member? {
        val sql = "SELECT * FROM member WHERE email = ?"
        return jdbc.queryForObject(sql, memberRowMapper, email)
    }

    override fun save(member: Member): Member? {
        val parameters =
            mapOf(
                "email" to member.email,
                "password" to member.password,
                "role" to member.role,
            )
        val generatedId = insert.executeAndReturnKey(parameters).toLong()
        return member.copy(id = generatedId)
    }

    override fun updateById(
        id: Long,
        member: Member,
    ): Member? {
        val sql = "UPDATE member SET email = ?, password = ?, role = ? WHERE id = ?"
        val updated = jdbc.update(sql, member.email, member.password, member.role, id)
        return if (updated > 0) member.copy(id = id) else null
    }

    override fun deleteById(id: Long): Boolean {
        val sql = "DELETE FROM member WHERE id = ?"
        return jdbc.update(sql, id) > 0
    }

    override fun existsByEmail(email: String): Boolean {
        val sql = "SELECT COUNT(*) FROM member WHERE email = ?"
        val count = jdbc.queryForObject(sql, Long::class.java, email) ?: 0
        return count > 0
    }
}
