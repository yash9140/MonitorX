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

data class ApiLogDto(
    val serviceName: String,
    val method: String,
    val endpoint: String,
    val timestamp: Instant,
    val latency: Long,
    val statusCode: Int,
    val requestSize: Long = 0,
    val responseSize: Long = 0
)

@Component
class ApiTrackingInterceptor(
    @Value("\${monitorx.client.service-name:unknown-service}")
    private val serviceName: String,
    @Value("\${monitorx.client.collector-url:http://localhost:8080}")
    private val collectorUrl: String,
    @Value("\${monitorx.client.enabled:true}")
    private val enabled: Boolean
) : HandlerInterceptor {

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    companion object {
        private const val START_TIME_ATTR = "monitorx.startTime"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (enabled) {
            request.setAttribute(START_TIME_ATTR, System.currentTimeMillis())
        }
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (!enabled) return

        val startTime = request.getAttribute(START_TIME_ATTR) as? Long ?: return
        val latency = System.currentTimeMillis() - startTime

        val apiLog = ApiLogDto(
            serviceName = serviceName,
            method = request.method,
            endpoint = request.requestURI,
            timestamp = Instant.now(),
            latency = latency,
            statusCode = response.status,
            requestSize = request.contentLengthLong.takeIf { it > 0 } ?: 0,
            responseSize = 0 // Response size tracking would need additional logic
        )

        // Send async
        Thread {
            try {
                sendLogToCollector(apiLog)
            } catch (e: Exception) {
                // Silent fail - don't impact the main request
                println("Failed to send log to collector: ${e.message}")
            }
        }.start()
    }

    private fun sendLogToCollector(apiLog: ApiLogDto) {
        val requestBody = objectMapper.writeValueAsString(apiLog)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$collectorUrl/collector/logs"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }
}
