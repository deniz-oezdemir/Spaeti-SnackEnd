package ecommerce.entities

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
import java.time.LocalDateTime

@Entity
@Table(
    name = "cart_item",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "product_id"])],
)
class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "added_at", nullable = false)
    val addedAt: LocalDateTime,
) {
    fun updateQuantity(newQuantity: Int) {
        require(newQuantity > 0) { "Quantity must be greater than 0." }
        this.quantity = newQuantity
    }
}
