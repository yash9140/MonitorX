package com.monitorx.service

import com.monitorx.dto.ApiLogRequest
import com.monitorx.model.ApiLog
import com.monitorx.repository.logs.ApiLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class LogIngestionService(
    private val apiLogRepository: ApiLogRepository,
    private val alertingService: AlertingService
) {

    fun ingestLog(request: ApiLogRequest): ApiLog {
        val apiLog = ApiLog(
            serviceName = request.serviceName,
            method = request.method,
            endpoint = request.endpoint,
            timestamp = request.timestamp,
            latency = request.latency,
            statusCode = request.statusCode,
            requestSize = request.requestSize,
            responseSize = request.responseSize
        )

        val savedLog = apiLogRepository.save(apiLog)

        // Process alerts asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alertingService.processLog(savedLog)
            } catch (e: Exception) {
                // Log error but don't fail the request
                println("Error processing alerts for log ${savedLog.id}: ${e.message}")
            }
        }

        return savedLog
    }
}
