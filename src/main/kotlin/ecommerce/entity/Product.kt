package ecommerce.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "products")
class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String,
    @Column(nullable = false)
    var price: Double,
    @Column(nullable = false, name = "image_url")
    var imageUrl: String,
    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.MERGE, CascadeType.PERSIST],
        orphanRemoval = true,
    )
    val options: MutableList<Option> = mutableListOf(),

    // NEW: enables optimistic locking via Hibernate/JPA
    @Version
    var version: Long? = null
) {
    init {
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(price >= 0.01) { "Price must be positive" }
        require(imageUrl.isNotBlank()) { "Image URL (or key) must not be blank" }
    }

    @PrePersist
    @PreUpdate
    fun validateOptionsNotEmpty() {
        require(options.isNotEmpty()) { "A product must have at least one option" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Product) return false

        if (this.id != null && other.id != null) {
            return this.id == other.id
        }

        return false
    }

    override fun hashCode(): Int = id?.hashCode() ?: super.hashCode()
}
