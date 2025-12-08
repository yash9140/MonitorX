package com.monitorx.client

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@ConditionalOnProperty(prefix = "monitorx.client", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class TrackingClientConfig(
    private val apiTrackingInterceptor: ApiTrackingInterceptor,
    private val rateLimiterInterceptor: RateLimiterInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimiterInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/actuator/**", "/error")
        
        registry.addInterceptor(apiTrackingInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/actuator/**", "/error")
    }
}
