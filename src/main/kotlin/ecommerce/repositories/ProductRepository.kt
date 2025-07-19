package ecommerce.repositories

import ecommerce.entities.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class ProductRepository(private val jdbc: JdbcTemplate) {
    private val productRowMapper =
        RowMapper<Product> { rs: ResultSet, _: Int ->
            Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("imageUrl"),
            )
        }

    fun findAll(): List<Product> {
        val sql = "SELECT * FROM PRODUCT"
        return jdbc.query(sql, productRowMapper)
    }

    fun findById(id: Long): Product? {
        val sql = "SELECT * from PRODUCT where ID = ?"
        return jdbc.queryForObject(sql, productRowMapper, id)
    }

    fun save(product: Product): Product? {
        val sql = "INSERT INTO PRODUCT (name, price, imageUrl) VALUES (?, ?, ?)"
        val keyHolder = GeneratedKeyHolder()

        val rows =
            jdbc.update({
                it.prepareStatement(sql, arrayOf("id")).apply {
                    setString(1, product.name)
                    setDouble(2, product.price)
                    setString(3, product.imageUrl)
                }
            }, keyHolder)

        if (rows == 0) return null
        val id = keyHolder.key?.toLong() ?: return null
        return product.copy(id = id)
    }

    fun updateById(
        id: Long,
        product: Product,
    ): Product? {
        val sql = "UPDATE PRODUCT SET name = ?, price = ?, imageUrl = ? WHERE id = ?"
        val rows = jdbc.update(sql, product.name, product.price, product.imageUrl, id)
        return if (rows > 0) product.copy(id = id) else null
    }

    fun deleteById(id: Long): Boolean {
        val sql = "DELETE FROM PRODUCT WHERE ID = ?"
        val rows = jdbc.update(sql, id)
        return rows > 0
    }

    fun deleteAll(): Boolean {
        val sql = "DELETE FROM PRODUCT"
        val rows = jdbc.update(sql)
        return rows > 0
    }
}
