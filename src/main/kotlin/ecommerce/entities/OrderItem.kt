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

@Entity
@Table(name = "order_item")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,
    @Column(nullable = false)
    val productName: String,
    @Column(nullable = false)
    val optionName: String,
    @Column(nullable = false)
    val price: Double,
    @Column(nullable = false)
    val quantity: Long,
)
