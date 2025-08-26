package ecommerce.concurrency

import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
class OptimisticLockIntegrationTest
    @Autowired
    constructor(
        private val optionRepo: OptionRepositoryJpa,
        private val productRepo: ProductRepositoryJpa,
        private val txManager: PlatformTransactionManager,
    ) {
        @Test
        fun `optimistic lock prevents lost update`() {
            // --- given: a valid product WITH an option (quantity = 2) ---
            val product =
                Product(
                    name = "Optimistic Test Product",
                    price = 9.99,
                    imageUrl = "img",
                )
            val option =
                Option(
                    product = product,
                    name = "Default",
                    quantity = 2,
                )
            product.options.add(option)
            val savedProduct = productRepo.save(product)
            val optionId = savedProduct.options.first().id!!

            val tx1 = TransactionTemplate(txManager)
            val tx2 = TransactionTemplate(txManager)

            // --- both transactions read the same row (capturing the same initial version) ---
            val stale1 = tx1.execute { optionRepo.findById(optionId).get() }!!
            val stale2 = tx2.execute { optionRepo.findById(optionId).get() }!!

            // --- Tx1 updates first and commits (version increments) ---
            tx1.execute {
                stale1.decreaseQuantity(1)
                optionRepo.saveAndFlush(stale1)
            }

            // --- Tx2 attempts to update using its STALE copy -> should fail with optimistic lock error ---
            assertThatThrownBy {
                tx2.execute {
                    stale2.decreaseQuantity(1)
                    optionRepo.saveAndFlush(stale2) // uses stale version
                }
            }.isInstanceOf(OptimisticLockingFailureException::class.java)

            // sanity: after Tx1, quantity is 1
            val after = optionRepo.findById(optionId).get()
            assertEquals(1, after.quantity)
        }
    }
