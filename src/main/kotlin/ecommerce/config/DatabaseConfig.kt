package ecommerce.config

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig {
    @Bean
    fun databaseInitializer(databaseSeeder: DatabaseSeeder): CommandLineRunner =
        CommandLineRunner {
            databaseSeeder.seed()
        }
}
