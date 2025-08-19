package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    val password: String,
    @Column(nullable = false)
    val role: String,
    @Column(nullable = false)
    var name: String,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    val products: MutableList<Product> = mutableListOf(),
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id",
        referencedColumnName = "member_id",
        insertable = false,
        updatable = false,
    )
    val cart: Cart? = null,
)
