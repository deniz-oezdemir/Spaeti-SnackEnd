package ecommerce.repositories

import ecommerce.entities.CartItem
import ecommerce.entities.Product
import ecommerce.model.ActiveMemberDTO
import ecommerce.model.TopProductDTO
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class CartItemRepositoryImpl(private val jdbc: JdbcTemplate) : CartItemRepository {
    private val cartItemWithProductRowMapper =
        RowMapper<Pair<CartItem, Product>> { rs: ResultSet, _: Int ->
            val cartItem =
                CartItem(
                    id = rs.getLong("cart_id"),
                    memberId = rs.getLong("member_id"),
                    productId = rs.getLong("product_id"),
                    quantity = rs.getInt("quantity"),
                    addedAt = rs.getTimestamp("added_at").toLocalDateTime(),
                )

            val product =
                Product(
                    id = rs.getLong("product_id"),
                    name = rs.getString("product_name"),
                    price = rs.getDouble("product_price"),
                    imageUrl = rs.getString("product_image_url"),
                )

            cartItem to product
        }

    private val insert: SimpleJdbcInsert by lazy {
        SimpleJdbcInsert(jdbc)
            .withTableName("cart_item")
            .usingGeneratedKeyColumns("id")
            .usingColumns("member_id", "product_id", "quantity") // no added_at here
    }

    override fun findByMember(memberId: Long): List<Pair<CartItem, Product>> {
        val sql =
            """
            SELECT ci.id AS cart_id,
                   ci.member_id,
                   ci.product_id,
                   ci.quantity,
                   ci.added_at,
                   p.id AS product_id,
                   p.name AS product_name,
                   p.price AS product_price,
                   p.image_url AS product_image_url
            FROM cart_item ci
            JOIN product p ON ci.product_id = p.id
            WHERE ci.member_id = ?
            """.trimIndent()

        return jdbc.query(sql, cartItemWithProductRowMapper, memberId)
    }

    override fun existsByProductAndMember(
        productId: Long,
        memberId: Long,
    ): Boolean {
        val sql = "SELECT COUNT(*) FROM cart_item WHERE product_id = ? AND member_id = ?"
        return jdbc.queryForObject(sql, Long::class.java, productId, memberId)!! > 0
    }

    override fun create(cartItem: CartItem): Pair<CartItem, Product>? {
        val params =
            mapOf(
                "member_id" to cartItem.memberId,
                "product_id" to cartItem.productId,
                "quantity" to cartItem.quantity,
            )

        val id = insert.executeAndReturnKey(params).toLong()

        val sql =
            """
            SELECT ci.id AS cart_id,
                     ci.member_id,
                     ci.product_id,
                     ci.quantity,
                     ci.added_at,
                     p.id AS product_id,
                     p.name AS product_name,
                     p.price AS product_price,
                     p.image_url AS product_image_url
            FROM cart_item ci
            JOIN product p ON ci.product_id = p.id
            WHERE ci.id = ?
            """.trimIndent()
        return jdbc.queryForObject(sql, cartItemWithProductRowMapper, id)
    }

    override fun update(cartItem: CartItem): Pair<CartItem, Product>? {
        val sql =
            """
            UPDATE cart_item
            SET quantity = ?,
                added_at = CASE WHEN ? > quantity THEN CURRENT_TIMESTAMP ELSE added_at END
            WHERE product_id = ? AND member_id = ?
            """.trimIndent()

        val updated =
            jdbc.update(
                sql,
                cartItem.quantity,
                cartItem.quantity,
                cartItem.productId,
                cartItem.memberId,
            )

        if (updated == 0) return null

        val sqlSelect =
            """
            SELECT ci.id AS cart_id,
                   ci.member_id,
                   ci.product_id,
                   ci.quantity,
                   ci.added_at,
                   p.id AS product_id,
                   p.name AS product_name,
                   p.price AS product_price,
                   p.image_url AS product_image_url
            FROM cart_item ci
            JOIN product p ON ci.product_id = p.id
            WHERE ci.product_id = ? AND ci.member_id = ?
            """.trimIndent()

        return jdbc.queryForObject(sqlSelect, cartItemWithProductRowMapper, cartItem.productId, cartItem.memberId)
    }

    override fun deleteByProduct(cartItem: CartItem): Boolean {
        val sql = "DELETE FROM cart_item WHERE product_id = ? And member_id = ?"
        return jdbc.update(sql, cartItem.productId, cartItem.memberId) > 0
    }

    override fun findDistinctMembersWithCartActivityInLast7Days(): List<ActiveMemberDTO> {
        val sql =
            """
            SELECT DISTINCT m.id, m.email, m.name
            FROM cart_item c
            JOIN member m ON c.member_id = m.id
            WHERE c.added_at >= DATEADD('DAY', -7, CURRENT_TIMESTAMP)
            """.trimIndent()

        return jdbc.query(sql) { rs, _ ->
            ActiveMemberDTO(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
            )
        }
    }

    override fun findTop5ProductsAddedInLast30Days(): List<TopProductDTO> {
        val sql =
            """
            SELECT p.name,
                   COUNT(*) AS added_count,
                   MAX(c.added_at) AS most_recent_added_at
            FROM cart_item c
            JOIN product p ON c.product_id = p.id
            WHERE c.added_at >=  DATEADD('DAY', -30, CURRENT_TIMESTAMP)
            GROUP BY c.product_id, p.name
            ORDER BY added_count DESC, most_recent_added_at DESC
            LIMIT 5
            """.trimIndent()

        return jdbc.query(sql) { rs, _ ->
            TopProductDTO(
                name = rs.getString("name"),
                count = rs.getInt("added_count"),
                mostRecentAddedAt = rs.getTimestamp("most_recent_added_at").toLocalDateTime(),
            )
        }
    }

    override fun deleteAll() {
        val sql = "DELETE FROM CART_ITEM"
        jdbc.update(sql)
    }
}
