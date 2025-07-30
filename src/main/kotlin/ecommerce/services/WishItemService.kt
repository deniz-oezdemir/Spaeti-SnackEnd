package ecommerce.services

import ecommerce.entities.WishItem
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toDto
import ecommerce.mappers.toEntity
import ecommerce.model.WishItemRequestDTO
import ecommerce.model.WishItemResponseDTO
import ecommerce.model.MemberDTO
import ecommerce.repositories.WishItemRepository
import ecommerce.repositories.ProductRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class WishItemService(
    private val wishItemRepository: WishItemRepository,
    private val productRepository: ProductRepository,
) {
    fun save(
        wishItemRequestDTO: WishItemRequestDTO,
        member: MemberDTO,
    ): WishItemResponseDTO {
        validateProductExists(wishItemRequestDTO.productId)

        val product = productRepository.findByIdOrNull(wishItemRequestDTO.productId)
        if (product == null) throw OperationFailedException("Invalid Product Id ${wishItemRequestDTO.productId}")
        return wishItemRepository.save(
            WishItem(
                member = member.toEntity(),
                product = product,
                addedAt = LocalDateTime.now(),
            ),
        ).toDto()
    }

    fun findByMember(memberId: Long): List<WishItemResponseDTO> {
        val wishItems = wishItemRepository.findByMemberId(memberId)

        return wishItems.map { cartItem ->
            WishItemResponseDTO(
                id = cartItem.id!!,
                memberId = cartItem.member.id!!,
                product = cartItem.product.toDTO(),
                addedAt = cartItem.addedAt,
            )
        }
    }

    fun findByMember(memberId: Long, page: Pageable): Page<WishItemResponseDTO> {
        val wishItems = wishItemRepository.findByMemberId(memberId, page)

        return wishItems.map { cartItem ->
            WishItemResponseDTO(
                id = cartItem.id!!,
                memberId = cartItem.member.id!!,
                product = cartItem.product.toDTO(),
                addedAt = cartItem.addedAt,
            )
        }
    }

    fun delete(
        wishItemRequestDTO: WishItemRequestDTO,
        memberId: Long,
    ) {
        wishItemRepository.deleteByProductIdAndMemberId(wishItemRequestDTO.productId, memberId)
    }

    private fun validateProductExists(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw EmptyResultDataAccessException("Product with ID $productId does not exist", 1)
        }
    }

    fun deleteAll() {
        wishItemRepository.deleteAll()
    }
}
