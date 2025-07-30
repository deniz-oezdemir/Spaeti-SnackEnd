package ecommerce.services

import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductService {
    fun findAll(pageable: Pageable): Page<ProductDTO>

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

    fun validateProductNameUniqueness(name: String)
}
