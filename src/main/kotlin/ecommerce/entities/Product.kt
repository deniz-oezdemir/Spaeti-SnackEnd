package ecommerce.entities

import ecommerce.exception.InvalidOptionNameException
import ecommerce.mappers.toEntity
import ecommerce.model.OptionDTO
import ecommerce.model.ProductPatchDTO
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

    fun copyFrom(
        other: ProductRequestDTO,
        optionDTOs: Set<OptionDTO> = emptySet(),
    ) {
        this.name = other.name
        this.price = other.price
        this.imageUrl = other.imageUrl
        this.options = mapOptionDTOs(optionDTOs)
    }

    fun copyFrom(
        other: ProductPatchDTO,
        optionDTOs: Set<OptionDTO> = emptySet(),
    ) {
        other.name?.let { if (it.isNotBlank()) this.name = it }
        other.price?.let { this.price = it }
        other.imageUrl?.let { if (it.isNotBlank()) this.imageUrl = it }
        if (optionDTOs.isNotEmpty()) {
            this.options = mapOptionDTOs(optionDTOs)
        }
    }

    private fun mapOptionDTOs(optionDTOs: Set<OptionDTO>): List<Option> {
        return optionDTOs.map { dto ->
            _options.find { it.id == dto.id }?.apply {
                updateName(dto.name)
                updateQuantity(dto.quantity)
            } ?: dto.toEntity(this)
        }
    }
}
