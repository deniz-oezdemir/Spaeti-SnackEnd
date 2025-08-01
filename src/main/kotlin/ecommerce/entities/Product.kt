package ecommerce.entities

import ecommerce.mappers.toEntity
import ecommerce.model.OptionDTO
import ecommerce.model.ProductRequestDTO
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
    private val _options: MutableList<Option> = mutableListOf()
) {
    var options: List<Option>
        get() = _options.toList()
        set(value) {
            _options.clear()
            _options.addAll(value)
        }

    fun addOption(option: Option) {
        if (_options.any { it.name == option.name }) {
            throw IllegalArgumentException("Option with name '${option.name}' already exists")
        }
        _options.add(option)
    }

    fun copyFrom(other: ProductRequestDTO, optionDTOs: Set<OptionDTO> = emptySet()) {
        this.name = other.name
        this.price = other.price
        this.imageUrl = other.imageUrl

        options = optionDTOs.map { optionDTO ->
            val option = _options
                .find { it.id == optionDTO.id }
                ?.apply {
                    name = optionDTO.name
                    quantity = optionDTO.quantity
                }
                ?: optionDTO.toEntity(this)

            option
        }
    }
}
