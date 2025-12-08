package com.monitorx.dto

import com.monitorx.model.AlertType
import com.monitorx.model.IssueStatus
import com.monitorx.model.IssueType
import java.time.Instant

data class ApiLogRequest(
    val serviceName: String,
    val method: String,
    val endpoint: String,
    val timestamp: Instant = Instant.now(),
    val latency: Long,
    val statusCode: Int,
    val requestSize: Long = 0,
    val responseSize: Long = 0
)

data class RateLimitEventRequest(
    val serviceName: String,
    val currentRate: Int,
    val limit: Int
)

data class LogsFilterRequest(
    val serviceName: String? = null,
    val endpoint: String? = null,
    val minStatusCode: Int? = null,
    val maxStatusCode: Int? = null,
    val slowOnly: Boolean = false, // latency > 500ms
    val brokenOnly: Boolean = false, // 5xx
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class AlertsFilterRequest(
    val alertType: AlertType? = null,
    val serviceName: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class IssuesFilterRequest(
    val status: IssueStatus? = null,
    val serviceName: String? = null,
    val issueType: IssueType? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class ResolveIssueRequest(
    val resolvedBy: String
)

data class StatsResponse(
    val slowApiCount: Long,
    val brokenApiCount: Long,
    val rateLimitViolations: Long,
    val averageLatency: Double,
    val topSlowEndpoints: List<TopEndpoint> = emptyList(),
    val errorRateTimeSeries: List<TimeSeries> = emptyList()
)

data class TopEndpoint(
    val serviceName: String,
    val endpoint: String,
    val averageLatency: Double,
    val hitCount: Long
)

data class TimeSeries(
    val timestamp: Instant,
    val errorCount: Long,
    val totalCount: Long,
    val errorRate: Double
)
