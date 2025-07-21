package ecommerce.repositories

import ecommerce.entities.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class ProductRepositoryImpl(private val jdbc: JdbcTemplate) : ProductRepository {
    private val productRowMapper =
        RowMapper<Product> { rs: ResultSet, _: Int ->
            Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("imageUrl"),
            )
        }

    override fun countAll(): Int {
        val sql = "SELECT COUNT(*) FROM PRODUCT"
        return jdbc.queryForObject(sql, Int::class.java) ?: 0
    }

    override fun findAll(): List<Product> {
        val sql = "SELECT * FROM PRODUCT"
        return jdbc.query(sql, productRowMapper)
    }

    override fun findAllPaginated(
        offset: Int,
        size: Int,
    ): List<Product> {
        val sql = "SELECT * FROM PRODUCT ORDER BY ID LIMIT ? OFFSET ?"
        return jdbc.query(sql, productRowMapper, size, offset)
    }

    override fun findById(id: Long): Product? {
        val sql = "SELECT * from PRODUCT where ID = ?"
        return jdbc.queryForObject(sql, productRowMapper, id)
    }

    override fun save(product: Product): Product? {
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

    override fun updateById(
        id: Long,
        product: Product,
    ): Product? {
        val sql = "UPDATE PRODUCT SET name = ?, price = ?, imageUrl = ? WHERE id = ?"
        val rows = jdbc.update(sql, product.name, product.price, product.imageUrl, id)
        return if (rows > 0) product.copy(id = id) else null
    }

    override fun deleteById(id: Long): Boolean {
        val sql = "DELETE FROM PRODUCT WHERE ID = ?"
        val rows = jdbc.update(sql, id)
        return rows > 0
    }

    override fun deleteAll(): Boolean {
        val sql = "DELETE FROM PRODUCT"
        val rows = jdbc.update(sql)
        return rows > 0
    }

    override fun existsByName(name: String): Boolean {
        val sql = "SELECT COUNT(*) FROM PRODUCT WHERE name = ?"
        return jdbc.queryForObject(sql, Long::class.java, name)!! > 0
    }
}
