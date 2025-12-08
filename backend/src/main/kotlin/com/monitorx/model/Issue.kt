package com.monitorx.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

enum class IssueStatus {
    OPEN,
    RESOLVED
}

enum class IssueType {
    SLOW_API,
    BROKEN_API,
    RATE_LIMIT
}

@Document(collection = "issues")
@CompoundIndex(
    name = "unique_open_issue_idx",
    def = "{'serviceName': 1, 'endpoint': 1, 'issueType': 1, 'status': 1}",
    unique = true,
    partialFilter = "{ 'status': 'OPEN' }"
)
data class Issue(
    @Id
    val id: String? = null,
    
    @Indexed
    val serviceName: String,
    
    val endpoint: String,
    
    val issueType: IssueType,
    
    @Indexed
    val status: IssueStatus = IssueStatus.OPEN,
    
    val hitCount: Int = 1, // Number of times this issue occurred
    
    val firstSeenAt: Instant = Instant.now(),
    
    val lastSeenAt: Instant = Instant.now(),
    
    val resolvedAt: Instant? = null,
    
    val resolvedBy: String? = null,
    
    @Version
    val version: Long? = null // For optimistic locking
)
