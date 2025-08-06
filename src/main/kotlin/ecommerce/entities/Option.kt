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
    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MIN_QUANTITY = 1L
        const val MAX_QUANTITY = 99_999_999L
    }

    @Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
    var name: String = name
        private set

    @Column(name = "quantity", nullable = false)
    var quantity: Long = quantity
        private set

    init {
        updateName(name)
        updateQuantity(quantity)
    }

    fun updateName(newName: String) {
        validateName(newName)
        this.name = newName
    }

    fun updateQuantity(newQuantity: Long) {
        validateQuantity(newQuantity)
        this.quantity = newQuantity
    }

    fun subtract(quantity: Long) {
        if (quantity < MIN_QUANTITY) throw InvalidOptionQuantityException("Subtract amount must be >= $MIN_QUANTITY")
        if (this.quantity < quantity) throw InsufficientStockException("Not enough stock")
        this.quantity -= quantity
    }

    private fun validateName(name: String) {
        if (name.length > MAX_NAME_LENGTH) throw InvalidOptionNameException("Option name too long")
        val allowed = Regex("^[\\p{Alnum} \\(\\)\\[\\]\\+\\-\\&\\/_]+$")
        if (!allowed.matches(name)) throw InvalidOptionNameException("Option names contains invalid characters: '$name'")
    }

    private fun validateQuantity(quantity: Long) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw InvalidOptionQuantityException("Quantity must be between $MIN_QUANTITY and $MAX_QUANTITY")
        }
    }
}
