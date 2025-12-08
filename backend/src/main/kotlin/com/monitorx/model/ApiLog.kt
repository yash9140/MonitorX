package com.monitorx.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "api_logs")
@CompoundIndex(name = "service_endpoint_idx", def = "{'serviceName': 1, 'endpoint': 1}")
@CompoundIndex(name = "timestamp_idx", def = "{'timestamp': -1}")
data class ApiLog(
    @Id
    val id: String? = null,
    
    @Indexed
    val serviceName: String,
    
    val method: String, // HTTP method (GET, POST, etc.)
    
    val endpoint: String,
    
    @Indexed
    val timestamp: Instant = Instant.now(),
    
    val latency: Long, // in milliseconds
    
    @Indexed
    val statusCode: Int,
    
    val requestSize: Long = 0, // in bytes
    
    val responseSize: Long = 0 // in bytes
)
