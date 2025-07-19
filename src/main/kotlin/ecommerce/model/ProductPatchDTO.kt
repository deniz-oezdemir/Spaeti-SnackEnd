package ecommerce.model

data class ProductPatchDTO(
    var id: Long? = null,
    var name: String? = null,
    var price: Double? = null,
    var imageUrl: String? = null,
)
