package com.monitorx.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(
    basePackages = ["com.monitorx.repository.logs"],
    mongoTemplateRef = "logsMongoTemplate"
)
class LogsMongoConfig {

    @Bean(name = ["logsMongoProperties"])
    @ConfigurationProperties(prefix = "spring.data.mongodb.logs")
    fun logsMongoProperties(): MongoProperties {
        return MongoProperties()
    }

    @Bean(name = ["logsMongoClient"])
    fun logsMongoClient(@Qualifier("logsMongoProperties") properties: MongoProperties): MongoClient {
        return MongoClients.create(properties.uri)
    }

    @Bean(name = ["logsMongoDatabaseFactory"])
    fun logsMongoDatabaseFactory(
        @Qualifier("logsMongoClient") mongoClient: MongoClient,
        @Qualifier("logsMongoProperties") properties: MongoProperties
    ): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(mongoClient, properties.uri.split("/").last().split("?").first())
    }

    @Bean(name = ["logsMongoTemplate"])
    fun logsMongoTemplate(@Qualifier("logsMongoDatabaseFactory") databaseFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(databaseFactory)
    }
}

@Configuration
@EnableMongoRepositories(
    basePackages = ["com.monitorx.repository.metadata"],
    mongoTemplateRef = "metadataMongoTemplate"
)
class MetadataMongoConfig {

    @Bean(name = ["metadataMongoProperties"])
    @Primary
    @ConfigurationProperties(prefix = "spring.data.mongodb.metadata")
    fun metadataMongoProperties(): MongoProperties {
        return MongoProperties()
    }

    @Bean(name = ["metadataMongoClient"])
    @Primary
    fun metadataMongoClient(@Qualifier("metadataMongoProperties") properties: MongoProperties): MongoClient {
        return MongoClients.create(properties.uri)
    }

    @Bean(name = ["metadataMongoDatabaseFactory"])
    @Primary
    fun metadataMongoDatabaseFactory(
        @Qualifier("metadataMongoClient") mongoClient: MongoClient,
        @Qualifier("metadataMongoProperties") properties: MongoProperties
    ): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(mongoClient, properties.uri.split("/").last().split("?").first())
    }

    @Bean(name = ["metadataMongoTemplate"])
    @Primary
    fun metadataMongoTemplate(@Qualifier("metadataMongoDatabaseFactory") databaseFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(databaseFactory)
    }
}
