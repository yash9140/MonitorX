package com.monitorx.repository.metadata

import com.monitorx.model.Issue
import com.monitorx.model.IssueStatus
import com.monitorx.model.IssueType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IssueRepository : MongoRepository<Issue, String> {
    
    fun findByStatus(status: IssueStatus, pageable: Pageable): Page<Issue>
    
    fun findByServiceName(serviceName: String, pageable: Pageable): Page<Issue>
    
    fun findByServiceNameAndStatus(
        serviceName: String,
        status: IssueStatus,
        pageable: Pageable
    ): Page<Issue>
    
    fun findByServiceNameAndEndpointAndIssueTypeAndStatus(
        serviceName: String,
        endpoint: String,
        issueType: IssueType,
        status: IssueStatus
    ): Issue?
    
    fun findByIssueTypeAndStatus(issueType: IssueType, status: IssueStatus, pageable: Pageable): Page<Issue>
}
