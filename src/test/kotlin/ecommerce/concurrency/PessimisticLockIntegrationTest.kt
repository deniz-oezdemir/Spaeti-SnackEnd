package ecommerce.concurrency

import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class PessimisticLockIntegrationTest
    @Autowired
    constructor(
        private val optionRepo: OptionRepositoryJpa,
        private val productRepo: ProductRepositoryJpa,
        private val txManager: PlatformTransactionManager,
    ) {
        @Test
        fun `pessimistic lock prevents overselling`() {
            // --- given: a VALID product WITH one option (qty = 1) ---
            val product = Product(name = "Test", price = 10.0, imageUrl = "img")
            val option = Option(product = product, name = "Default", quantity = 1)
            product.options.add(option) // satisfy domain rule "A product must have at least one option"
            val savedProduct = productRepo.save(product)
            val optionId = savedProduct.options.first().id!!

            val threads = 2
            val exec = Executors.newFixedThreadPool(threads)
            val start = CountDownLatch(1)
            val results = mutableListOf<Boolean>()
            val resultsLock = Any()

            repeat(threads) {
                exec.submit {
                    start.await()
                    val ok = decrementOneWithPessimisticLockTx(optionId)
                    synchronized(resultsLock) { results.add(ok) }
                }
            }

            start.countDown()
            exec.shutdown()
            exec.awaitTermination(5, TimeUnit.SECONDS)

            // --- then: exactly one success; final stock == 0; no negatives ---
            val refreshed = optionRepo.findById(optionId).get()
            assertEquals(0, refreshed.quantity, "Stock should be 0 after one successful decrement")
            assertEquals(1, results.count { it }, "Exactly one thread should succeed")
        }

        /**
         * Runs the read-modify-write WITHIN a real Spring transaction using TransactionTemplate.
         * This ensures the PESSIMISTIC_WRITE lock is actually held until commit.
         * @Transactional on a method inside the test class won’t work (self-invocation bypasses the Spring proxy), so use a TransactionTemplate
         */
        private fun decrementOneWithPessimisticLockTx(id: Long): Boolean {
            val tx = TransactionTemplate(txManager)
            return tx.execute {
                val opt = optionRepo.findWithLockById(id) ?: return@execute false
                if (opt.quantity <= 0) return@execute false
                opt.decreaseQuantity(1)
                optionRepo.saveAndFlush(opt) // push change before tx completes
                true
            } ?: false
        }
    }
