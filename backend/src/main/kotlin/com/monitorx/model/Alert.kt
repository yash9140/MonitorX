package com.monitorx.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

enum class AlertType {
    SLOW_API,      // Latency > 500ms
    BROKEN_API,    // 5xx status
    RATE_LIMIT     // Rate limit exceeded
}

@Document(collection = "alerts")
data class Alert(
    @Id
    val id: String? = null,
    
    @Indexed
    val serviceName: String,
    
    val endpoint: String,
    
    @Indexed
    val alertType: AlertType,
    
    val reason: String,
    
    @Indexed
    val timestamp: Instant = Instant.now()
)
