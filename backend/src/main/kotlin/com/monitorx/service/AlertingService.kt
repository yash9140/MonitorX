package com.monitorx.service

import com.monitorx.model.*
import com.monitorx.repository.metadata.AlertRepository
import com.monitorx.repository.metadata.IssueRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AlertingService(
    private val alertRepository: AlertRepository,
    private val issueRepository: IssueRepository
) {

    fun processLog(apiLog: ApiLog) {
        // Check for slow API (latency > 500ms)
        if (apiLog.latency > 500) {
            createAlert(
                serviceName = apiLog.serviceName,
                endpoint = apiLog.endpoint,
                alertType = AlertType.SLOW_API,
                reason = "API latency ${apiLog.latency}ms exceeds threshold of 500ms"
            )
            createOrUpdateIssue(
                serviceName = apiLog.serviceName,
                endpoint = apiLog.endpoint,
                issueType = IssueType.SLOW_API
            )
        }

        // Check for broken API (5xx status)
        if (apiLog.statusCode >= 500) {
            createAlert(
                serviceName = apiLog.serviceName,
                endpoint = apiLog.endpoint,
                alertType = AlertType.BROKEN_API,
                reason = "API returned ${apiLog.statusCode} error status"
            )
            createOrUpdateIssue(
                serviceName = apiLog.serviceName,
                endpoint = apiLog.endpoint,
                issueType = IssueType.BROKEN_API
            )
        }
    }

    fun processRateLimitEvent(event: RateLimitEvent) {
        createAlert(
            serviceName = event.serviceName,
            endpoint = "N/A",
            alertType = AlertType.RATE_LIMIT,
            reason = "Rate limit exceeded: ${event.currentRate} req/s (limit: ${event.limit})"
        )
        createOrUpdateIssue(
            serviceName = event.serviceName,
            endpoint = "N/A",
            issueType = IssueType.RATE_LIMIT
        )
    }

    private fun createAlert(
        serviceName: String,
        endpoint: String,
        alertType: AlertType,
        reason: String
    ) {
        val alert = Alert(
            serviceName = serviceName,
            endpoint = endpoint,
            alertType = alertType,
            reason = reason
        )
        alertRepository.save(alert)
    }

    private fun createOrUpdateIssue(
        serviceName: String,
        endpoint: String,
        issueType: IssueType
    ) {
        // Find existing OPEN issue
        val existingIssue = issueRepository.findByServiceNameAndEndpointAndIssueTypeAndStatus(
            serviceName = serviceName,
            endpoint = endpoint,
            issueType = issueType,
            status = IssueStatus.OPEN
        )

        if (existingIssue != null) {
            // Update existing issue
            val updatedIssue = existingIssue.copy(
                hitCount = existingIssue.hitCount + 1,
                lastSeenAt = Instant.now()
            )
            issueRepository.save(updatedIssue)
        } else {
            // Create new issue
            val newIssue = Issue(
                serviceName = serviceName,
                endpoint = endpoint,
                issueType = issueType,
                status = IssueStatus.OPEN,
                hitCount = 1
            )
            issueRepository.save(newIssue)
        }
    }
}
