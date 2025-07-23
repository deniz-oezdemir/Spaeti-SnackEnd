package ecommerce.mappers

import ecommerce.entities.Member
import ecommerce.model.MemberDTO

fun Member.toDto() = MemberDTO(id, email, password)

fun MemberDTO.toEntity() = Member(id, email, password)