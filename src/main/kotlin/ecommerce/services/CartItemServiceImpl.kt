package ecommerce.services

import ecommerce.entities.CartItem
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toDto
import ecommerce.mappers.toEntity
import ecommerce.model.CartItemRequestDTO
import ecommerce.model.CartItemResponseDTO
import ecommerce.model.MemberDTO
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.ProductRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CartItemServiceImpl(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) : CartItemService {
    @Transactional
    override fun addOrUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        member: MemberDTO,
    ): CartItemResponseDTO {
        validateProductExists(cartItemRequestDTO.productId)

        val cartItem =
            if (!cartItemRepository.existsByProductIdAndMemberId(cartItemRequestDTO.productId, member.id!!)) {
                handleCreate(cartItemRequestDTO, member)
            } else {
                handleUpdate(cartItemRequestDTO, member)
            }

        return cartItem.toDto()
    }

    @Transactional(readOnly = true)
    override fun findByMember(memberId: Long): List<CartItemResponseDTO> {
        val itemsWithProducts = cartItemRepository.findByMemberId(memberId)

        return itemsWithProducts.map { cartItem ->
            CartItemResponseDTO(
                id = cartItem.id!!,
                memberId = cartItem.member.id!!,
                product = cartItem.product.toDTO(),
                quantity = cartItem.quantity,
                addedAt = cartItem.addedAt,
            )
        }
    }

    @Transactional
    override fun delete(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ) {
        cartItemRepository.deleteByProductIdAndMemberId(cartItemRequestDTO.productId, memberId)
    }

    private fun validateProductExists(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw EmptyResultDataAccessException("Product with ID $productId does not exist", 1)
        }
    }

    @Transactional
    override fun deleteAll() {
        cartItemRepository.deleteAll()
    }

    private fun handleCreate(
        cartItemRequestDTO: CartItemRequestDTO,
        member: MemberDTO,
    ): CartItem {
        val product = productRepository.findByIdOrNull(cartItemRequestDTO.productId)
        if (product == null) throw OperationFailedException("Invalid Product Id ${cartItemRequestDTO.productId}")
        return cartItemRepository.save(
            CartItem(
                member = member.toEntity(),
                product = product,
                quantity = if (cartItemRequestDTO.quantity == 0) 1 else cartItemRequestDTO.quantity,
                addedAt = LocalDateTime.now(),
            ),
        )
    }

    private fun handleUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        member: MemberDTO,
    ): CartItem {
        val existing =
            cartItemRepository
                .findByProductIdAndMemberId(cartItemRequestDTO.productId, member.id!!)
                ?: throw OperationFailedException("Cart item not found")

        if (cartItemRequestDTO.quantity <= 0) throw OperationFailedException("Quantity must be greater than zero")
        if (existing.quantity == cartItemRequestDTO.quantity) return existing

        existing.updateQuantity(cartItemRequestDTO.quantity)
        return cartItemRepository.save(existing)
    }
}
