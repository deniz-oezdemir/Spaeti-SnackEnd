package ecommerce.mappers

import ecommerce.entities.Member
import ecommerce.model.MemberDTO

fun Member.toResponseDto() = MemberDTO(id, name, email, password, role)

fun MemberDTO.toEntity() = Member(id, name, email, password, role)
