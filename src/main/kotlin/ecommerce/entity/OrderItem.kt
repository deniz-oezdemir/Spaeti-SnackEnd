package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    val order: Order,
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_option_id")
    val productOption: Option,
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false)
    val price: Double,
    @Column(name = "product_name", nullable = false)
    val productName: String,
    @Column(name = "option_name", nullable = false)
    val optionName: String,
    @Column(name = "product_image_url", nullable = false)
    val productImageUrl: String,
)
