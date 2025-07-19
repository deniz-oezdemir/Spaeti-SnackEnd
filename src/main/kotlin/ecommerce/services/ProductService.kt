package ecommerce.services

import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO

interface ProductService {
    fun findAll(): List<ProductDTO>

    fun findById(id: Long): ProductDTO

    fun save(productDTO: ProductDTO): ProductDTO

    fun updateById(
        id: Long,
        productDTO: ProductDTO,
    ): ProductDTO?

    fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductDTO?

    fun deleteById(id: Long)

    fun deleteAll()
}
