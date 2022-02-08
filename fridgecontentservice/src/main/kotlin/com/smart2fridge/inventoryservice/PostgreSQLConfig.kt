package com.smart2fridge.inventoryservice

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
/*
    Contains a configuration factory to be used to connect to a postgres database.

    @Configuration indicates that this class contains beans to be picked up by spring dependency injection
    @Profile ensures that this configuration is only registered if the default profile is active, hence it won't be picked up in testing mode, where a h2 in-memory database is used
    @Value properties are loaded from the active configuration
 */
@Configuration
@Profile("default")
class PostgreSQLConfig(
    @Value("\${postgres.url}") val url: String,
    @Value("\${postgres.port}") val port: Int,
    @Value("\${postgres.username}") val username: String,
    @Value("\${postgres.password}") val password: String,
    @Value("\${postgres.db}") val database: String,
) {
    @Bean
    fun postgreSQLConnectionFactory(): PostgresqlConnectionFactory = PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host(url)
            .port(port)
            .username(username)
            .password(password)
            .database(database)
            .build()
    )
}