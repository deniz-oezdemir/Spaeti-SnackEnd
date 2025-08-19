package ecommerce.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "options",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_option_product_name",
            columnNames = ["product_id", "name"],
        ),
    ],
)
class Option(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @field:JsonIgnore
    val product: Product,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var quantity: Long,
) {
    init {
        require(name.length <= 50) { "Name must not exceed 50 characters" }
        require(name.matches(Regex("^[\\p{L}\\p{N}\\s()\\[\\]+\\-&/_]*\$"))) {
            "Invalid characters in option name"
        }

        require(quantity in 1..99_999_999) {
            "Quantity must be between 1 and 99,999,999"
        }
    }

    fun decreaseQuantity(amount: Long) {
        require(amount > 0) { "Amount must be positive" }
        check(quantity >= amount) { "Insufficient stock for option id=$id" }
        quantity -= amount
    }
}
