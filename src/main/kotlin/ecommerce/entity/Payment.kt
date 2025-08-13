package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "payments")
class Payment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    @Column(nullable = false)
    val stripeSessionId: String,
    @Column(nullable = false)
    val amount: Long,
    @Column(nullable = false)
    val currency: String,
    @Column(nullable = false)
    val status: String,
    @Column(nullable = true)
    val paymentMethod: String? = null,
    @Column(nullable = true)
    val failureReason: String? = null,
)
