package com.monitorx.controller

import com.monitorx.dto.ApiLogRequest
import com.monitorx.dto.RateLimitEventRequest
import com.monitorx.model.ApiLog
import com.monitorx.model.RateLimitEvent
import com.monitorx.service.LogIngestionService
import com.monitorx.service.RateLimitService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/collector")
class CollectorController(
    private val logIngestionService: LogIngestionService,
    private val rateLimitService: RateLimitService
) {

    @PostMapping("/logs")
    fun ingestLog(@RequestBody request: ApiLogRequest): ResponseEntity<ApiLog> {
        val log = logIngestionService.ingestLog(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(log)
    }

    @PostMapping("/rate-limit-events")
    fun recordRateLimitEvent(@RequestBody request: RateLimitEventRequest): ResponseEntity<RateLimitEvent> {
        val event = rateLimitService.recordRateLimitEvent(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(event)
    }
}
