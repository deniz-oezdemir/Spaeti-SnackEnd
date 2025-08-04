package ecommerce.mappers

import ecommerce.entities.Product
import ecommerce.model.ProductPatchDTO
import ecommerce.model.ProductRequestDTO
import ecommerce.model.ProductResponseDTO

fun Product.toDTO(): ProductResponseDTO = ProductResponseDTO(id, name, price, imageUrl, options = options.map { it.toDTO() })

fun ProductRequestDTO.toEntity(): Product = Product(id, name, price, imageUrl)

fun ProductResponseDTO.toEntity(): Product = Product(id, name, price, imageUrl)

fun ProductRequestDTO.toEntityWithOptions(): Product {
    val product = this.toEntity()
    val optionEntities = this.options.map { it.toEntity(product) }
    product.options = optionEntities
    return product
}

fun Product.applyPatchFromDTO(patch: ProductPatchDTO) {
    patch.name?.let { if (it.isNotBlank()) this.name = it }
    patch.price?.let { this.price = it }
    patch.imageUrl?.let { if (it.isNotBlank()) this.imageUrl = it }
    if (patch.options.isNotEmpty()) {
        val updatedOptions =
            patch.options.map { dto ->
                this.options.find { it.id == dto.id }?.apply {
                    updateName(dto.name)
                    updateQuantity(dto.quantity)
                } ?: dto.toEntity(this)
            }
        this.options = updatedOptions
    }
}
