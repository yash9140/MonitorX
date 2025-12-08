package com.monitorx.service

import com.monitorx.dto.AlertsFilterRequest
import com.monitorx.dto.LogsFilterRequest
import com.monitorx.model.Alert
import com.monitorx.model.ApiLog
import com.monitorx.repository.logs.ApiLogRepository
import com.monitorx.repository.metadata.AlertRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Service

@Service
class QueryService(
    private val apiLogRepository: ApiLogRepository,
    private val alertRepository: AlertRepository,
    @Qualifier("logsMongoTemplate") private val logsMongoTemplate: MongoTemplate
) {

    fun queryLogs(filter: LogsFilterRequest): Page<ApiLog> {
        val criteria = mutableListOf<Criteria>()

        filter.serviceName?.let {
            criteria.add(Criteria.where("serviceName").`is`(it))
        }

        filter.endpoint?.let {
            criteria.add(Criteria.where("endpoint").regex(it, "i"))
        }

        filter.minStatusCode?.let {
            criteria.add(Criteria.where("statusCode").gte(it))
        }

        filter.maxStatusCode?.let {
            criteria.add(Criteria.where("statusCode").lte(it))
        }

        if (filter.slowOnly) {
            criteria.add(Criteria.where("latency").gt(500))
        }

        if (filter.brokenOnly) {
            criteria.add(Criteria.where("statusCode").gte(500))
        }

        filter.startDate?.let {
            criteria.add(Criteria.where("timestamp").gte(it))
        }

        filter.endDate?.let {
            criteria.add(Criteria.where("timestamp").lte(it))
        }

        val query = Query()
        if (criteria.isNotEmpty()) {
            query.addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
        }

        val pageable = PageRequest.of(
            filter.page,
            filter.size,
            Sort.by(Sort.Direction.DESC, "timestamp")
        )

        query.with(pageable)

        val logs = logsMongoTemplate.find(query, ApiLog::class.java)
        val count = logsMongoTemplate.count(query.skip(0).limit(0), ApiLog::class.java)

        return PageableExecutionUtils.getPage(logs, pageable) { count }
    }

    fun queryAlerts(filter: AlertsFilterRequest): Page<Alert> {
        val pageable = PageRequest.of(
            filter.page,
            filter.size,
            Sort.by(Sort.Direction.DESC, "timestamp")
        )

        return when {
            filter.alertType != null && filter.serviceName != null -> {
                alertRepository.findByServiceNameAndAlertType(
                    filter.serviceName,
                    filter.alertType,
                    pageable
                )
            }
            filter.alertType != null && filter.startDate != null && filter.endDate != null -> {
                alertRepository.findByAlertTypeAndTimestampBetween(
                    filter.alertType,
                    filter.startDate,
                    filter.endDate,
                    pageable
                )
            }
            filter.alertType != null -> {
                alertRepository.findByAlertType(filter.alertType, pageable)
            }
            filter.serviceName != null -> {
                alertRepository.findByServiceName(filter.serviceName, pageable)
            }
            filter.startDate != null && filter.endDate != null -> {
                alertRepository.findByTimestampBetween(filter.startDate, filter.endDate, pageable)
            }
            else -> {
                alertRepository.findAll(pageable)
            }
        }
    }
}
