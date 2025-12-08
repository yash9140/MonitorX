package com.monitorx.repository.metadata

import com.monitorx.model.Alert
import com.monitorx.model.AlertType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AlertRepository : MongoRepository<Alert, String> {
    
    fun findByAlertType(alertType: AlertType, pageable: Pageable): Page<Alert>
    
    fun findByServiceName(serviceName: String, pageable: Pageable): Page<Alert>
    
    fun findByServiceNameAndAlertType(
        serviceName: String,
        alertType: AlertType,
        pageable: Pageable
    ): Page<Alert>
    
    @Query("{ 'timestamp': { '\$gte': ?0, '\$lte': ?1 } }")
    fun findByTimestampBetween(start: Instant, end: Instant, pageable: Pageable): Page<Alert>
    
    fun findByAlertTypeAndTimestampBetween(
        alertType: AlertType,
        start: Instant,
        end: Instant,
        pageable: Pageable
    ): Page<Alert>
}
