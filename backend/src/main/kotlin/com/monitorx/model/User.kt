package com.monitorx.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String, // BCrypt hashed
    val role: String = "developer",
    val createdAt: Instant = Instant.now()
)
