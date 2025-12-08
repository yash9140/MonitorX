package com.monitorx.controller

import com.monitorx.dto.StatsResponse
import com.monitorx.dto.TimeSeries
import com.monitorx.dto.TopEndpoint
import com.monitorx.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stats")
class StatsController(
    private val statsService: StatsService
) {

    @GetMapping("/summary")
    fun getStatsSummary(): ResponseEntity<StatsResponse> {
        try {
            val stats = StatsResponse(
                slowApiCount = statsService.getSlowApiCount(),
                brokenApiCount = statsService.getBrokenApiCount(),
                rateLimitViolations = statsService.getRateLimitViolationCount(),
                averageLatency = statsService.getAverageLatency(),
                topSlowEndpoints = statsService.getTopSlowEndpoints(10),
                errorRateTimeSeries = statsService.getErrorRateTimeSeriesSimple(24)
            )
            return ResponseEntity.ok(stats)
        } catch (e: Exception) {
            // Log error but return empty stats instead of 500
            println("Error loading stats summary: ${e.message}")
            e.printStackTrace()
            val emptyStats = StatsResponse(
                slowApiCount = 0,
                brokenApiCount = 0,
                rateLimitViolations = 0,
                averageLatency = 0.0,
                topSlowEndpoints = emptyList(),
                errorRateTimeSeries = emptyList()
            )
            return ResponseEntity.ok(emptyStats)
        }
    }

    @GetMapping("/slow-api-count")
    fun getSlowApiCount(): ResponseEntity<Map<String, Long>> {
        return ResponseEntity.ok(mapOf("count" to statsService.getSlowApiCount()))
    }

    @GetMapping("/broken-api-count")
    fun getBrokenApiCount(): ResponseEntity<Map<String, Long>> {
        return ResponseEntity.ok(mapOf("count" to statsService.getBrokenApiCount()))
    }

    @GetMapping("/rate-limit-violations")
    fun getRateLimitViolations(): ResponseEntity<Map<String, Long>> {
        return ResponseEntity.ok(mapOf("count" to statsService.getRateLimitViolationCount()))
    }

    @GetMapping("/average-latency")
    fun getAverageLatency(): ResponseEntity<Map<String, Double>> {
        return ResponseEntity.ok(mapOf("latency" to statsService.getAverageLatency()))
    }

    @GetMapping("/top-slow-endpoints")
    fun getTopSlowEndpoints(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<TopEndpoint>> {
        val topEndpoints = statsService.getTopSlowEndpoints(limit)
        return ResponseEntity.ok(topEndpoints)
    }

    @GetMapping("/error-rate-time-series")
    fun getErrorRateTimeSeries(
        @RequestParam(defaultValue = "24") hours: Int
    ): ResponseEntity<List<TimeSeries>> {
        val timeSeries = statsService.getErrorRateTimeSeriesSimple(hours)
        return ResponseEntity.ok(timeSeries)
    }
}
