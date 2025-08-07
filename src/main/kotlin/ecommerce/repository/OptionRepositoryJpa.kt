package ecommerce.repository

import ecommerce.entity.Option
import org.springframework.data.jpa.repository.JpaRepository

interface OptionRepositoryJpa : JpaRepository<Option, Long>
