package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(
    name = "option",
)
class Option(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
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
