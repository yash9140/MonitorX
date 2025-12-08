package com.monitorx.controller

import com.monitorx.dto.IssuesFilterRequest
import com.monitorx.dto.ResolveIssueRequest
import com.monitorx.model.Issue
import com.monitorx.model.IssueStatus
import com.monitorx.model.IssueType
import com.monitorx.service.IssueService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/issues")
class IssuesController(
    private val issueService: IssueService
) {

    @GetMapping
    fun getIssues(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) serviceName: String?,
        @RequestParam(required = false) issueType: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<Issue>> {
        val filter = IssuesFilterRequest(
            status = status?.let { IssueStatus.valueOf(it) },
            serviceName = serviceName,
            issueType = issueType?.let { IssueType.valueOf(it) },
            page = page,
            size = size
        )

        val issues = issueService.listIssues(filter)
        return ResponseEntity.ok(issues)
    }

    @GetMapping("/{id}")
    fun getIssueById(@PathVariable id: String): ResponseEntity<Issue> {
        val issue = issueService.getIssueById(id)
        return ResponseEntity.ok(issue)
    }

    @PutMapping("/{id}/resolve")
    fun resolveIssue(
        @PathVariable id: String,
        @RequestBody request: ResolveIssueRequest
    ): ResponseEntity<Issue> {
        val issue = issueService.resolveIssue(id, request.resolvedBy)
        return ResponseEntity.ok(issue)
    }
}
