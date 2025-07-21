package ecommerce.services

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.repositories.ProductRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class ProductServiceJDBC(private val productRepository: ProductRepository) : ProductService {
    override fun findAll(): List<ProductDTO> = productRepository.findAll().map { it.toDTO() }

    override fun findAllPaginated(
        page: Int,
        size: Int,
    ): Pair<List<ProductDTO>, Int> {
        val offset = (page - 1).coerceAtLeast(0) * size
        val items = productRepository.findAllPaginated(offset, size).map { it.toDTO() }
        val totalCount = productRepository.countAll()
        return Pair(items, totalCount)
    }

    override fun findById(id: Long): ProductDTO =
        productRepository.findById(id)?.toDTO() ?: throw NotFoundException("Product with ID $id not found")

    override fun save(productDTO: ProductDTO): ProductDTO {
        val saved =
            productRepository.save(productDTO.toEntity())
                ?: throw OperationFailedException("Failed to save product")
        return saved.toDTO()
    }

    override fun updateById(
        id: Long,
        productDTO: ProductDTO,
    ): ProductDTO {
        return productRepository.updateById(id, productDTO.toEntity())?.toDTO()
            ?: throw NotFoundException("Product with ID $id not found")
    }

    override fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductDTO {
        val existing = findById(id)
        val updatedProduct = existing.copyFrom(productPatchDTO)
        return productRepository.updateById(id, updatedProduct.toEntity())?.toDTO()
            ?: throw NotFoundException("Product with ID $id not found")
    }

    override fun deleteById(id: Long) {
        if (productRepository.findById(id) == null) throw NotFoundException("Product with ID $id not found")
        if (!productRepository.deleteById(id)) {
            throw OperationFailedException("Failed to delete product with ID $id")
        }
    }

    override fun deleteAll() =
        if (!productRepository.deleteAll()) {
            throw OperationFailedException("Failed to delete all products")
        } else {
            Unit
        }
}
