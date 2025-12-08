package com.monitorx.controller

import com.monitorx.dto.LogsFilterRequest
import com.monitorx.model.ApiLog
import com.monitorx.service.QueryService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/logs")
class LogsController(
    private val queryService: QueryService
) {

    @GetMapping
    fun getLogs(
        @RequestParam(required = false) serviceName: String?,
        @RequestParam(required = false) endpoint: String?,
        @RequestParam(required = false) minStatusCode: Int?,
        @RequestParam(required = false) maxStatusCode: Int?,
        @RequestParam(required = false, defaultValue = "false") slowOnly: Boolean,
        @RequestParam(required = false, defaultValue = "false") brokenOnly: Boolean,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ApiLog>> {
        val filter = LogsFilterRequest(
            serviceName = serviceName,
            endpoint = endpoint,
            minStatusCode = minStatusCode,
            maxStatusCode = maxStatusCode,
            slowOnly = slowOnly,
            brokenOnly = brokenOnly,
            startDate = startDate?.let { java.time.Instant.parse(it) },
            endDate = endDate?.let { java.time.Instant.parse(it) },
            page = page,
            size = size
        )

        val logs = queryService.queryLogs(filter)
        return ResponseEntity.ok(logs)
    }
}
