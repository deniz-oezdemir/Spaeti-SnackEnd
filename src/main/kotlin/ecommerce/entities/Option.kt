package ecommerce.entities

import ecommerce.exception.InsufficientStockException
import ecommerce.exception.InvalidOptionNameException
import ecommerce.exception.InvalidOptionQuantityException
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
    name = "`option`",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_id", "name"])],
)
class Option(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    name: String,
    quantity: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product? = null,
) {
    @Column(name = "name", nullable = false, length = 50)
    var name: String = name
        set(value) {
            validateName(value)
            field = value
        }

    @Column(name = "quantity", nullable = false)
    var quantity: Long = quantity
        set(value) {
            validateQuantity(value)
            field = value
        }

    init {
        this.name = name
        this.quantity = quantity
    }

    fun subtract(quantity: Long) {
        if (quantity < 1) throw InvalidOptionQuantityException("Subtract amount must be >= 1")
        if (this.quantity < quantity) throw InsufficientStockException("Not enough stock")
        this.quantity -= quantity
    }

    private fun validateName(name: String) {
        if (name.length > 50) throw InvalidOptionNameException("Option name too long")
        val allowed = Regex("^[\\p{Alnum} \\(\\)\\[\\]\\+\\-\\&\\/_]+$")
        if (!allowed.matches(name)) throw InvalidOptionNameException("Option names contains invalid characters: '$name'")
    }

    private fun validateQuantity(quantity: Long) {
        if (quantity < 1 || quantity >= 100_000_000) throw InvalidOptionQuantityException("Quantity must be between 1 and 99,999,999")
    }
}
