package com.monitorx.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "rate_limit_events")
data class RateLimitEvent(
    @Id
    val id: String? = null,
    val serviceName: String,
    val timestamp: Instant = Instant.now(),
    val currentRate: Int, // requests per second
    val limit: Int // configured limit
)
