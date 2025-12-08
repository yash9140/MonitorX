package com.monitorx.client

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class RateLimitEventDto(
    val serviceName: String,
    val currentRate: Int,
    val limit: Int
)

@Component
class RateLimiterInterceptor(
    @Value("\${monitorx.client.service-name:unknown-service}")
    private val serviceName: String,
    @Value("\${monitorx.client.collector-url:http://localhost:8080}")
    private val collectorUrl: String,
    @Value("\${monitorx.client.rate-limit:100}")
    private val rateLimitPerSecond: Int,
    @Value("\${monitorx.client.rate-limit-enabled:true}")
    private val enabled: Boolean
) : HandlerInterceptor {

    private val requestCounts = ConcurrentHashMap<Long, AtomicInteger>()
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (!enabled) return true

        val currentSecond = System.currentTimeMillis() / 1000
        val count = requestCounts.computeIfAbsent(currentSecond) { AtomicInteger(0) }
        val currentRate = count.incrementAndGet()

        // Clean up old entries
        val now = Instant.now().epochSecond
        requestCounts.keys.removeIf { it < now - 10 }

        // Check if rate limit exceeded (non-blocking, just track)
        if (currentRate > rateLimitPerSecond) {
            val event = RateLimitEventDto(
                serviceName = serviceName,
                currentRate = currentRate,
                limit = rateLimitPerSecond
            )

            // Send async
            Thread {
                try {
                    sendRateLimitEvent(event)
                } catch (e: Exception) {
                    println("Failed to send rate limit event: ${e.message}")
                }
            }.start()
        }

        // Non-blocking: always allow the request to proceed
        return true
    }

    private fun sendRateLimitEvent(event: RateLimitEventDto) {
        val requestBody = objectMapper.writeValueAsString(event)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$collectorUrl/collector/rate-limit-events"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }
}
