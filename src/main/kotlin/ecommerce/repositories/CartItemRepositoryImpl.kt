package ecommerce.repositories

import ecommerce.entities.CartItem
import ecommerce.entities.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class CartItemRepositoryImpl(private val jdbc: JdbcTemplate) : CartItemRepository {
    private val cartItemRowMapper =
        RowMapper<CartItem> { rs: ResultSet, _: Int ->
            CartItem(
                id = rs.getLong("id"),
                memberId = rs.getLong("member_id"),
                productId = rs.getLong("product_id"),
                quantity = rs.getInt("quantity"),
                addedAt = rs.getTimestamp("added_at").toLocalDateTime(),
            )
        }

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

    override fun existsByProduct(productId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM CART_ITEM WHERE PRODUCT_ID = ?"
        return jdbc.queryForObject(sql, Long::class.java, productId)!! > 0
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
                   p.image_url AS product_imageUrl
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
}
