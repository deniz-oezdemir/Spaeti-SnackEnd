package ecommerce.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "`order`")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    val payment: Payment,
    @Column(nullable = false)
    val orderDate: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,
) {
    enum class OrderStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELLED,
    }

    fun addOrderItem(orderItem: OrderItem) {
        items.add(orderItem)
        orderItem.order = this
    }
}
