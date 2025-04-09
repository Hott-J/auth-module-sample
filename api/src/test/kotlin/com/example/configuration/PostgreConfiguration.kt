package com.example.configuration

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class PostgreConfiguration : ApplicationContextInitializer<ConfigurableApplicationContext>, Closeable {

    private val logger = KotlinLogging.logger { }
    private var container: PostgreSQLContainer<*>? = null

    companion object {
        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .apply { start() }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        logger.info { "Initializing Postgres test container" }

        TestPropertyValues.of(
            "spring.datasource.url=${postgres.jdbcUrl}",
            "spring.datasource.username=${postgres.username}",
            "spring.datasource.password=${postgres.password}",
            "spring.datasource.driver-class-name=org.postgresql.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
        ).applyTo(applicationContext.environment)

        this.container = postgres
    }

    override fun close() {
        container?.close()
    }
}
