package ecommerce.config

import ecommerce.entities.CartItem
import ecommerce.entities.Member
import ecommerce.entities.Option
import ecommerce.entities.Product
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
class DatabaseConfig(
    private val productRepository: ProductRepository,
    private val memberRepository: MemberRepository,
    private val cartItemRepository: CartItemRepository,
) {
    @Bean
    fun databaseInitializer(): CommandLineRunner =
        CommandLineRunner {
            val products =
                listOf(
                    Product(
                        name = "Car",
                        price = 1000.0,
                        imageUrl = "https://images.unsplash.com/photo-1494905998402-395d579af36f?w=400&h=400&fit=crop",
                    ),
                    Product(
                        name = "Bike",
                        price = 200.0,
                        imageUrl = "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400&h=400&fit=crop",
                    ),
                    Product(
                        name = "Truck",
                        price = 30000.0,
                        imageUrl = "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&h=400&fit=crop",
                    ),
                    Product(
                        name = "Laptop",
                        price = 1500.0,
                        imageUrl = "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop",
                    ),
                    Product(
                        name = "Phone",
                        price = 800.0,
                        imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop",
                    ),
                ) +
                    (6..25).map { i ->
                        Product(
                            name = "Product $i",
                            price = (i * 11.11),
                            imageUrl = "https://placeholder.vn/placeholder/400x400?bg=ff7f50&color=ffffff&text=Product$i",
                        )
                    }
            val savedProducts = productRepository.saveAll(products)

            // Add members
            val admin =
                Member(
                    name = "sebas",
                    email = "sebas@sebas.com",
                    password = "123456",
                    role = Member.Role.ADMIN,
                )
            val customers =
                (1..10).map { i ->
                    Member(
                        name = "User $i",
                        email = "user$i@example.com",
                        password = "pass",
                        role = Member.Role.CUSTOMER,
                    )
                }
            val savedMembers = memberRepository.saveAll(listOf(admin) + customers)

            // Add cart items
            val cartItems =
                listOf(
                    CartItem(
                        member = savedMembers[1],
                        product = savedProducts[5],
                        quantity = 1,
                        addedAt = LocalDateTime.now(),
                    ),
                    CartItem(
                        member = savedMembers[1],
                        product = savedProducts[6],
                        quantity = 2,
                        addedAt = LocalDateTime.now(),
                    ),
                    // Add more cart items as needed
                )
            cartItemRepository.saveAll(cartItems)

            // Add options
            val carOptions =
                listOf(
                    Option(name = "Red Color", quantity = 5, product = savedProducts[0]),
                    Option(name = "Blue Color", quantity = 3, product = savedProducts[0]),
                    Option(
                        name = "Black Color",
                        quantity = 4,
                        product = savedProducts[0],
                    ),
                    Option(
                        name = "Automatic Transmission",
                        quantity = 2,
                        product = savedProducts[0],
                    ),
                    Option(
                        name = "Manual Transmission",
                        quantity = 3,
                        product = savedProducts[0],
                    ),
                )

            val bikeOptions =
                listOf(
                    Option(
                        name = "Small Size",
                        quantity = 10,
                        product = savedProducts[1],
                    ),
                    Option(
                        name = "Medium Size",
                        quantity = 15,
                        product = savedProducts[1],
                    ),
                    Option(
                        name = "Large Size",
                        quantity = 8,
                        product = savedProducts[1],
                    ),
                    Option(
                        name = "Red Color",
                        quantity = 12,
                        product = savedProducts[1],
                    ),
                    Option(
                        name = "Black Color",
                        quantity = 7,
                        product = savedProducts[1],
                    ),
                )

            // Add options to products
            savedProducts[0].options = carOptions
            savedProducts[1].options = bikeOptions
            productRepository.saveAll(listOf(savedProducts[0], savedProducts[1]))
        }
}
