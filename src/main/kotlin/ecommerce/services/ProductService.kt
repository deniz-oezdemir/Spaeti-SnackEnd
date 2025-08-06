package ecommerce.services

import ecommerce.model.OptionDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.model.ProductRequestDTO
import ecommerce.model.ProductResponseDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductService {
    fun findAll(pageable: Pageable): Page<ProductResponseDTO>

    fun findById(id: Long): ProductResponseDTO

    fun findOptionsByProductId(productId: Long): List<OptionDTO>

    fun save(productRequestDTO: ProductRequestDTO): ProductResponseDTO

    fun updateById(
        id: Long,
        productDTO: ProductRequestDTO,
    ): ProductResponseDTO

    fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductResponseDTO

    fun deleteById(id: Long)

    fun deleteAll()

    fun validateProductNameUniqueness(name: String)
}
