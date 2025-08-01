package ecommerce.repositories

import ecommerce.entities.Option
import org.springframework.data.jpa.repository.JpaRepository

interface OptionRepository : JpaRepository<Option, Long>
