package ecommerce.services

import ecommerce.exception.NotFoundException
import ecommerce.model.ProductDTO
import ecommerce.model.ProductPatchDTO
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class ProductServiceCollection : ProductService {
    private val products = mutableMapOf<Long, ProductDTO>()
    private val index = AtomicLong(1)

    init {
        preloadProducts()
    }

    override fun findAll(): List<ProductDTO> = products.values.toList()

    override fun findAllPaginated(page: Int, size: Int): Pair<List<ProductDTO>, Int> {
        val offset = (page - 1).coerceAtLeast(0) * size
        val items = products.values
            .sortedBy { it.id }
            .drop(offset)
            .take(size)
        val totalCount = products.size
        return Pair(items.toList(), totalCount)
    }

    override fun findById(id: Long): ProductDTO =
        products[id] ?: throw NotFoundException("Product with ID $id not found")

    override fun save(productDTO: ProductDTO): ProductDTO {
        val id = index.getAndIncrement()
        val saved = productDTO.copy(id)
        products[id] = saved
        return saved
    }

    override fun updateById(
        id: Long,
        productDTO: ProductDTO,
    ): ProductDTO {
        val existing = findById(id)
        val updated = existing.copyFrom(productDTO)
        products[id] = updated
        return updated
    }

    override fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductDTO {
        val existing = findById(id)
        val updated = existing.copyFrom(productPatchDTO)
        products[id] = updated
        return updated
    }

    override fun deleteById(id: Long) {
        if (products.remove(id) != null) throw NotFoundException("Product with ID $id not found")
    }

    override fun deleteAll() {
        products.clear()
    }

    private fun preloadProducts() {
        listOf(
            ProductDTO(index.getAndIncrement(), "Car", 1000.0, "www.some.com"),
            ProductDTO(index.getAndIncrement(), "Bike", 200.0, "www.example.com/bike"),
            ProductDTO(index.getAndIncrement(), "Truck", 30000.0, "www.example.com/truck"),
        ).forEach { products[it.id!!] = it }
    }
}
