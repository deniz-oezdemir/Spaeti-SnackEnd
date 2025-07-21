package ecommerce.mappers

import ecommerce.entities.Product
import ecommerce.model.ProductDTO

fun Product.toDTO(): ProductDTO = ProductDTO(id, name, price, imageUrl)

fun ProductDTO.toEntity(): Product = Product(id, name, price, imageUrl)
