package ecommerce.mappers

import ecommerce.entities.Option
import ecommerce.entities.Product
import ecommerce.model.OptionDTO

fun Option.toDTO(): OptionDTO = OptionDTO(id, name, quantity, product?.id)

fun OptionDTO.toEntity(product: Product): Option = Option(id, name, quantity, product)
