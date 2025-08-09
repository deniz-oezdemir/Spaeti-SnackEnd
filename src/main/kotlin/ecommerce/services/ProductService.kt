package ecommerce.services

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.applyPatchFromDTO
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.mappers.toEntityWithOptions
import ecommerce.model.OptionDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.model.ProductRequestDTO
import ecommerce.model.ProductResponseDTO
import ecommerce.repositories.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<ProductResponseDTO> {
        val products = productRepository.findAll(pageable)
        return products.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): ProductResponseDTO {
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("Product with id=$id not found") }
        return product.toDTO()
    }

    @Transactional(readOnly = true)
    fun findOptionsByProductId(productId: Long): List<OptionDTO> {
        val product = productRepository.findById(productId)
            .orElseThrow { NotFoundException("Product with id=$productId not found") }
        return product.options.map { it.toDTO() }
    }

    @Transactional
    fun save(productRequestDTO: ProductRequestDTO): ProductResponseDTO {
        validateProductNameUniqueness(productRequestDTO.name)

        val product = productRequestDTO.toEntityWithOptions()
        val savedProduct = productRepository.save(product)
        return savedProduct.toDTO()
    }

    @Transactional
    fun updateById(
        id: Long,
        productDTO: ProductRequestDTO,
    ): ProductResponseDTO {
        val existing = productRepository.findById(id)
            .orElseThrow { NotFoundException("Product with id=$id not found") }

        if (existing.name != productDTO.name) {
            validateProductNameUniqueness(productDTO.name)
        }

        val options = productDTO.options.map { dto ->
            existing.options.find { it.id == dto.id }?.apply {
                updateName(dto.name)
                updateQuantity(dto.quantity)
            } ?: dto.toEntity(existing)
        }

        existing.applyUpdate(
            productDTO.name,
            productDTO.price,
            productDTO.imageUrl,
            options
        )

        return productRepository.save(existing).toDTO()
    }

    @Transactional
    fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductResponseDTO {
        val existing = productRepository.findById(id)
            .orElseThrow { NotFoundException("Product with id=$id not found") }
        if (productPatchDTO.name != null && existing.name != productPatchDTO.name) {
            validateProductNameUniqueness(productPatchDTO.name!!)
        }

        existing.applyPatchFromDTO(productPatchDTO)

        return productRepository.save(existing).toDTO()
    }

    @Transactional
    fun deleteById(id: Long) {
        if (!productRepository.existsById(id)) throw NotFoundException("Product with ID $id not found")
        productRepository.deleteById(id)
    }

    @Transactional
    fun deleteAll() {
        productRepository.deleteAll()
    }

    fun validateProductNameUniqueness(name: String) {
        if (productRepository.existsByName(name)) {
            throw OperationFailedException("Product with name '$name' already exists")
        }
    }
}
