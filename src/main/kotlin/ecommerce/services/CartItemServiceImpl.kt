package ecommerce.services

import ecommerce.entities.CartItem
import ecommerce.entities.Product
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toResponseDto
import ecommerce.model.CartItemRequestDTO
import ecommerce.model.CartItemResponseDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.ProductRepository
import org.springframework.stereotype.Service

@Service
class CartItemServiceImpl(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) : CartItemService {
    override fun addOrUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): CartItemResponseDTO {
        validateProductExists(cartItemRequestDTO.productId)

        val (cartItem, product) =
            if (!cartItemRepository.existsByProductAndMember(cartItemRequestDTO.productId, memberId)) {
                handleCreate(cartItemRequestDTO, memberId)
            } else {
                handleUpdate(cartItemRequestDTO, memberId)
            }

        return cartItem.toResponseDto(product.toDTO())
    }

    override fun findByMember(memberId: Long): List<CartItemResponseDTO> {
        val itemsWithProducts = cartItemRepository.findByMember(memberId)

        return itemsWithProducts.map { (cartItem, product) ->
            CartItemResponseDTO(
                id = cartItem.id!!,
                memberId = cartItem.memberId,
                product = product.toDTO(),
                quantity = cartItem.quantity,
                addedAt = cartItem.addedAt!!,
            )
        }
    }

    override fun delete(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ) {
        cartItemRepository.deleteByProduct(
            CartItem(
                memberId = memberId,
                productId = cartItemRequestDTO.productId,
                quantity = cartItemRequestDTO.quantity,
            ),
        )
    }

    private fun validateProductExists(productId: Long) {
        if (productRepository.findById(productId) == null) {
            throw NotFoundException("Product with id $productId not found")
        }
    }

    private fun handleCreate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): Pair<CartItem, Product> {
        return cartItemRepository.create(
            CartItem(
                memberId = memberId,
                productId = cartItemRequestDTO.productId,
                quantity = if (cartItemRequestDTO.quantity == 0) 1 else cartItemRequestDTO.quantity,
            ),
        ) ?: throw OperationFailedException()
    }

    private fun handleUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): Pair<CartItem, Product> {
        return cartItemRepository.update(
            CartItem(
                memberId = memberId,
                productId = cartItemRequestDTO.productId,
                quantity = cartItemRequestDTO.quantity,
            ),
        ) ?: throw OperationFailedException()
    }

    override fun deleteAll() {
        cartItemRepository.deleteAll()
    }
}
