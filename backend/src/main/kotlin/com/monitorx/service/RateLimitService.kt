package com.monitorx.service

import com.monitorx.dto.RateLimitEventRequest
import com.monitorx.model.RateLimitEvent
import com.monitorx.repository.logs.RateLimitEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class RateLimitService(
    private val rateLimitEventRepository: RateLimitEventRepository,
    private val alertingService: AlertingService
) {

    fun recordRateLimitEvent(request: RateLimitEventRequest): RateLimitEvent {
        val event = RateLimitEvent(
            serviceName = request.serviceName,
            currentRate = request.currentRate,
            limit = request.limit
        )

        val savedEvent = rateLimitEventRepository.save(event)

        // Trigger rate limit alert asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alertingService.processRateLimitEvent(savedEvent)
            } catch (e: Exception) {
                println("Error processing rate limit alert: ${e.message}")
            }
        }

        return savedEvent
    }

    fun getRateLimitViolationCount(): Long {
        return rateLimitEventRepository.count()
    }
}
