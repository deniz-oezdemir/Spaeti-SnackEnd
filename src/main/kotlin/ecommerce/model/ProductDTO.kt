package ecommerce.model

data class ProductDTO(
    var id: Long? = null,
//    @NotBlack
    var name: String,
    var price: Double,
    var imageUrl: String,
) {
    fun copyFrom(productDTO: ProductDTO): ProductDTO {
        productDTO.id?.let { this.id = it }
        this.name = productDTO.name
        this.price = productDTO.price
        this.imageUrl = productDTO.imageUrl
        return this
    }

    fun copyFrom(productPatchDTO: ProductPatchDTO): ProductDTO {
        productPatchDTO.id?.let { this.id = it }
        productPatchDTO.name?.takeIf { it.isNotBlank() }?.let { this.name = it }
        productPatchDTO.price?.let { this.price = it }
        productPatchDTO.imageUrl?.takeIf { it.isNotBlank() }?.let { this.imageUrl = it }
        return this
    }
}
