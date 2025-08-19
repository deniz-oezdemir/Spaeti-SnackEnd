package ecommerce.repository

import ecommerce.entity.Option
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface OptionRepositoryJpa : JpaRepository<Option, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): Option?
}
