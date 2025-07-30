package ecommerce.services

import ecommerce.entities.Product
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.repositories.ProductRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class ProductServiceImpl(private val productRepository: ProductRepository) : ProductService {
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<ProductDTO> {
        val products = productRepository.findAll(pageable)
        val productDTOs = products.map { it.toDTO() }
        return productDTOs
    }

    override fun findById(id: Long): ProductDTO {
        val product = productRepository.findByIdOrNull(id)
        if (product == null) throw NotFoundException("Product with id=$id not found")
        return product.toDTO()
    }

    override fun save(productDTO: ProductDTO): ProductDTO {
        validateProductNameUniqueness(productDTO.name)
        val product: Product = productRepository.save(productDTO.toEntity())
        return product.toDTO()
    }

    override fun updateById(
        id: Long,
        productDTO: ProductDTO,
    ): ProductDTO? {
        val originalProduct = findById(id)
        val newProduct = productDTO.copy(id = originalProduct.id)
        val updatedProduct = productRepository.save(newProduct.toEntity())
        return updatedProduct.toDTO()
    }

    override fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductDTO? {
        val existing = findById(id)
        val updatedProduct = existing.copyFrom(productPatchDTO)
        return productRepository.save(updatedProduct.toEntity()).toDTO()
    }

    override fun deleteById(id: Long) {
        if (!productRepository.existsById(id)) throw NotFoundException("Product with ID $id not found")
        productRepository.deleteById(id)
    }

    override fun deleteAll() {
        productRepository.deleteAll()
    }

    override fun validateProductNameUniqueness(name: String) {
        if (productRepository.existsByName(name)) {
            throw OperationFailedException("Product with name '$name' already exists")
        }
    }
}
