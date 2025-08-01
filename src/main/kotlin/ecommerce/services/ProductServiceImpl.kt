package ecommerce.services

import ecommerce.entities.Option
import ecommerce.entities.Product
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.model.ProductResponseDTO
import ecommerce.model.ProductPatchDTO
import ecommerce.model.ProductRequestDTO
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.ProductRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val optionRepository: OptionRepository
) : ProductService {
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<ProductResponseDTO> {
        val products = productRepository.findAll(pageable)
        val productDTOs = products.map { it.toDTO() }
        return productDTOs
    }

    override fun findById(id: Long): ProductResponseDTO {
        val product = productRepository.findByIdOrNull(id)

        if (product == null) throw NotFoundException("Product with id=$id not found")
        return product.toDTO()
    }

    override fun save(productRequestDTO: ProductRequestDTO): ProductResponseDTO {
        validateProductNameUniqueness(productRequestDTO.name)

        val product = productRequestDTO.toEntity()
        product.options = productRequestDTO.options.map { it.toEntity(product) }

        productRequestDTO.options.forEach { optionDTO ->
            val option = optionDTO.toEntity(product)
            product.addOption(option)
        }

        val savedProduct = productRepository.save(product)
        return savedProduct.toDTO()
    }

    override fun updateById(
        id: Long,
        productDTO: ProductRequestDTO,
    ): ProductResponseDTO? {
        val originalProduct = productRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Product with id=$id not found")

        if (originalProduct.name != productDTO.name) {
            validateProductNameUniqueness(productDTO.name)
        }

        originalProduct.copyFrom(productDTO, productDTO.options)

        return productRepository.save(originalProduct).toDTO()
    }

    override fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductResponseDTO? {
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
