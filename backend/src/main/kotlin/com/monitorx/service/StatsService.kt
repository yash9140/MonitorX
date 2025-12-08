package com.monitorx.service

import com.monitorx.dto.TimeSeries
import com.monitorx.dto.TopEndpoint
import com.monitorx.repository.logs.ApiLogRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class StatsService(
    private val apiLogRepository: ApiLogRepository,
    private val rateLimitService: RateLimitService,
    @Qualifier("logsMongoTemplate") private val logsMongoTemplate: MongoTemplate
) {

    fun getSlowApiCount(): Long {
        return try {
            logsMongoTemplate.count(
                Query.query(Criteria.where("latency").gt(500)),
                com.monitorx.model.ApiLog::class.java
            )
        } catch (e: Exception) {
            println("Error counting slow APIs: ${e.message}")
            e.printStackTrace()
            0L
        }
    }

    fun getBrokenApiCount(): Long {
        return try {
            logsMongoTemplate.count(
                Query.query(Criteria.where("statusCode").gte(500)),
                com.monitorx.model.ApiLog::class.java
            )
        } catch (e: Exception) {
            println("Error counting broken APIs: ${e.message}")
            e.printStackTrace()
            0L
        }
    }

    fun getRateLimitViolationCount(): Long {
        return rateLimitService.getRateLimitViolationCount()
    }

    fun getAverageLatency(): Double {
        return try {
            val groupStage = Aggregation.group()
                .avg("latency").`as`("avgLatency")

            val aggregation = Aggregation.newAggregation(groupStage)

            val results: AggregationResults<Map<String, Any>> =
                logsMongoTemplate.aggregate(aggregation, "api_logs", Map::class.java) as AggregationResults<Map<String, Any>>

            val avgLatency = results.mappedResults.firstOrNull()?.get("avgLatency") as? Number
            avgLatency?.toDouble() ?: 0.0
        } catch (e: Exception) {
            println("Error calculating average latency: ${e.message}")
            0.0
        }
    }

    fun getTopSlowEndpoints(limit: Int = 10): List<TopEndpoint> {
        try {
            val matchStage = Aggregation.match(Criteria.where("latency").gt(500))
            
            val groupStage = Aggregation.group("serviceName", "endpoint")
                .avg("latency").`as`("avgLatency")
                .count().`as`("hitCount")
            
            val sortStage = Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "avgLatency")
            
            val limitStage = Aggregation.limit(limit.toLong())
            
            val projectStage = Aggregation.project()
                .and("_id.serviceName").`as`("serviceName")
                .and("_id.endpoint").`as`("endpoint")
                .and("avgLatency").`as`("averageLatency")
                .and("hitCount").`as`("hitCount")

            val aggregation = Aggregation.newAggregation(
                matchStage,
                groupStage,
                sortStage,
                limitStage,
                projectStage
            )

            val results: AggregationResults<Map<String, Any>> = 
                logsMongoTemplate.aggregate(aggregation, "api_logs", Map::class.java) as AggregationResults<Map<String, Any>>

            return results.mappedResults.mapNotNull { doc ->
                try {
                    TopEndpoint(
                        serviceName = doc["serviceName"] as? String ?: "unknown",
                        endpoint = doc["endpoint"] as? String ?: "unknown",
                        averageLatency = (doc["averageLatency"] as? Number)?.toDouble() ?: 0.0,
                        hitCount = (doc["hitCount"] as? Number)?.toLong() ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            println("Error in getTopSlowEndpoints: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }


    fun getErrorRateTimeSeriesSimple(hours: Int = 24): List<TimeSeries> {
        try {
            val endDate = Instant.now()
            val startDate = endDate.minus(hours.toLong(), ChronoUnit.HOURS)
            
            // Simple implementation: get hourly buckets
            val timeSeries = mutableListOf<TimeSeries>()
            var currentTime = startDate
            
            while (currentTime.isBefore(endDate)) {
                val nextTime = currentTime.plus(1, ChronoUnit.HOURS)
                
                val logs = logsMongoTemplate.find(
                    Query.query(
                        Criteria.where("timestamp").gte(currentTime).lt(nextTime)
                    ),
                    com.monitorx.model.ApiLog::class.java
                )
                
                val totalCount = logs.size.toLong()
                val errorCount = logs.count { it.statusCode >= 500 }.toLong()
                val errorRate = if (totalCount > 0) errorCount.toDouble() / totalCount else 0.0
                
                timeSeries.add(
                    TimeSeries(
                        timestamp = currentTime,
                        errorCount = errorCount,
                        totalCount = totalCount,
                        errorRate = errorRate
                    )
                )
                
                currentTime = nextTime
            }
            
            return timeSeries
        } catch (e: Exception) {
            println("Error in getErrorRateTimeSeriesSimple: ${e.message}")
            return emptyList()
        }
    }
}
