package com.monitorx.service

import com.monitorx.dto.IssuesFilterRequest
import com.monitorx.model.Issue
import com.monitorx.model.IssueStatus
import com.monitorx.repository.metadata.IssueRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class IssueService(
    private val issueRepository: IssueRepository
) {

    fun listIssues(filter: IssuesFilterRequest): Page<Issue> {
        val pageable = PageRequest.of(
            filter.page,
            filter.size,
            Sort.by(Sort.Direction.DESC, "lastSeenAt")
        )

        return when {
            filter.serviceName != null && filter.status != null -> {
                issueRepository.findByServiceNameAndStatus(filter.serviceName, filter.status, pageable)
            }
            filter.serviceName != null -> {
                issueRepository.findByServiceName(filter.serviceName, pageable)
            }
            filter.status != null && filter.issueType != null -> {
                issueRepository.findByIssueTypeAndStatus(filter.issueType, filter.status, pageable)
            }
            filter.status != null -> {
                issueRepository.findByStatus(filter.status, pageable)
            }
            else -> {
                issueRepository.findAll(pageable)
            }
        }
    }

    fun resolveIssue(issueId: String, resolvedBy: String): Issue {
        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                val issue = issueRepository.findById(issueId)
                    .orElseThrow { IllegalArgumentException("Issue not found with id: $issueId") }

                if (issue.status == IssueStatus.RESOLVED) {
                    throw IllegalStateException("Issue is already resolved")
                }

                val resolvedIssue = issue.copy(
                    status = IssueStatus.RESOLVED,
                    resolvedAt = Instant.now(),
                    resolvedBy = resolvedBy
                )

                return issueRepository.save(resolvedIssue)
            } catch (e: OptimisticLockingFailureException) {
                retryCount++
                if (retryCount >= maxRetries) {
                    throw IllegalStateException("Failed to resolve issue due to concurrent modification. Please try again.")
                }
                // Wait a bit before retrying
                Thread.sleep(100)
            }
        }

        throw IllegalStateException("Failed to resolve issue after $maxRetries attempts")
    }

    fun getIssueById(issueId: String): Issue {
        return issueRepository.findById(issueId)
            .orElseThrow { IllegalArgumentException("Issue not found with id: $issueId") }
    }
}
