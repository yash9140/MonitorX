package com.monitorx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class MonitorXApplication

fun main(args: Array<String>) {
    runApplication<MonitorXApplication>(*args)
}
