package ecommerce.model

class Product(
    var id: Long? = null,
    var name: String? = null,
    var price: Double? = null,
    var imageUrl: String? = null,
) {
    fun update(newProduct: Product) {
        newProduct.name?.let { this.name = it }
        newProduct.price?.let { this.price = it }
        newProduct.imageUrl?.let { this.imageUrl = it }
    }

    companion object {
        fun toEntity(
            id: Long,
            product: Product,
        ): Product {
            return Product(id, product.name, product.price, product.imageUrl)
        }
    }

    init {
    }
}
