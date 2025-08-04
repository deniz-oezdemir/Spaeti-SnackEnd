package ecommerce.entities

import ecommerce.exception.InvalidOptionNameException
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "price", nullable = false)
    var price: Double,
    @Column(name = "image_url", nullable = false)
    var imageUrl: String,
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val cartItems: Set<CartItem> = emptySet(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "product", nullable = false)
    private val _options: MutableList<Option> = mutableListOf(),
) {
    var options: List<Option>
        get() = _options.toList()
        set(value) {
            _options.clear()
            _options.addAll(value)
        }

    fun addOption(option: Option) {
        if (_options.any { it.name == option.name }) {
            throw InvalidOptionNameException("Option with name '${option.name}' already exists")
        }
        option.product = this
        _options.add(option)
    }

    fun applyUpdate(
        name: String,
        price: Double,
        imageUrl: String,
        options: List<Option>,
    ) {
        this.name = name
        this.price = price
        this.imageUrl = imageUrl
        this.options = options
    }

    fun applyPatch(
        name: String?,
        price: Double?,
        imageUrl: String?,
        options: List<Option>?,
    ) {
        name?.let { if (it.isNotBlank()) this.name = it }
        price?.let { this.price = it }
        imageUrl?.let { if (it.isNotBlank()) this.imageUrl = it }
        if (options != null && options.isNotEmpty()) {
            this.options = options
        }
    }
}
