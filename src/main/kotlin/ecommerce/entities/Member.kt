package ecommerce.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "email", nullable = false)
    val email: String,
    @Column(name = "password", nullable = false)
    val password: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: Role = Role.CUSTOMER,
    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    val cartItems: Set<CartItem> = emptySet(),
) {
    enum class Role { CUSTOMER, ADMIN }
}
