package ecommerce.repositories

import ecommerce.entities.Option
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface OptionRepository : JpaRepository<Option, Long> {
    // Find an option by its ID and apply a pessimistic write lock preventing other transactions from modifying it until this one is complete.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Option o WHERE o.id = :id")
    fun findByIdWithLock(id: Long): Optional<Option>
}
