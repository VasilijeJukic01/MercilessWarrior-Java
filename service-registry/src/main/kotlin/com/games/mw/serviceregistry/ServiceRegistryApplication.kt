package com.games.mw.serviceregistry

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class ServiceRegistryApplication

fun main(args: Array<String>) {
    runApplication<ServiceRegistryApplication>(*args)
}
