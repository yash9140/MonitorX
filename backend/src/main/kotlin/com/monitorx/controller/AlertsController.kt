package com.monitorx.controller

import com.monitorx.dto.AlertsFilterRequest
import com.monitorx.model.Alert
import com.monitorx.model.AlertType
import com.monitorx.service.QueryService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/alerts")
class AlertsController(
    private val queryService: QueryService
) {

    @GetMapping
    fun getAlerts(
        @RequestParam(required = false) alertType: String?,
        @RequestParam(required = false) serviceName: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<Alert>> {
        val filter = AlertsFilterRequest(
            alertType = alertType?.let { AlertType.valueOf(it) },
            serviceName = serviceName,
            startDate = startDate?.let { Instant.parse(it) },
            endDate = endDate?.let { Instant.parse(it) },
            page = page,
            size = size
        )

        val alerts = queryService.queryAlerts(filter)
        return ResponseEntity.ok(alerts)
    }
}
