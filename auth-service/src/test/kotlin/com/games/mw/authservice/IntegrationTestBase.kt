package com.games.mw.authservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * An abstract base class for all integration tests in the auth-service.
 *
 * This class uses the Testcontainers extension to manage the lifecycle of external dependencies as Docker containers.
 * Kafka container is pointed itself as the schema registry for tests!
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
abstract class IntegrationTestBase {

    companion object {
        private val postgresImage = DockerImageName.parse("postgres:15.3")
        private val redisImage = DockerImageName.parse("redis:7-alpine")
        private val kafkaImage = DockerImageName.parse("confluentinc/cp-kafka:7.6.0")

        internal  val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(postgresImage)
            .withDatabaseName("mw-user-service-test")
            .withUsername("test")
            .withPassword("test")

        internal  val redisContainer: GenericContainer<*> = GenericContainer(redisImage)
            .withExposedPorts(6379)

        internal  val kafkaContainer = KafkaContainer(kafkaImage)

        init {
            postgresContainer.start()
            redisContainer.start()
            kafkaContainer.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)

            // Redis
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379).toString() }

            // Kafka
            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
            registry.add("spring.kafka.properties.schema.registry.url") { "mock://test-url" }
        }
    }
}