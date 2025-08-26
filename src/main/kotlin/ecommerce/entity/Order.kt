package ecommerce.entity

import ecommerce.enums.OrderStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import jakarta.validation.constraints.Email
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val memberId: Long,
    @Column(nullable = false)
    val orderDateTime: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    val items: MutableList<OrderItem> = mutableListOf(),
    @OneToOne(
        mappedBy = "order",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE],
        orphanRemoval = true,
    )
    var payment: Payment? = null,
    @Column(name = "is_gift", nullable = false)
    var isGift: Boolean = false,
    @Column(name = "gift_recipient_email")
    @field:Email
    var giftRecipientEmail: String? = null,
    @Column(name = "gift_message", length = 1000)
    var giftMessage: String? = null,
    @Column(name = "total_amount", precision = 12, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    // NEW: enables optimistic locking via Hibernate/JPA
    @Version
    var version: Long? = null
)
