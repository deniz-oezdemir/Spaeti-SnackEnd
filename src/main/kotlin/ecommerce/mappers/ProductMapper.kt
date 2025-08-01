package ecommerce.mappers

import ecommerce.entities.Product
import ecommerce.model.ProductRequestDTO
import ecommerce.model.ProductResponseDTO

fun Product.toDTO(): ProductResponseDTO = ProductResponseDTO(id, name, price, imageUrl, options = options.map { it.toDTO() })

fun ProductRequestDTO.toEntity(): Product = Product(id, name, price, imageUrl)

fun ProductResponseDTO.toEntity(): Product = Product(id, name, price, imageUrl)
