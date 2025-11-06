package com.games.mw.gameservice

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class GameServiceApplication

fun main(args: Array<String>) {
    runApplication<GameServiceApplication>(*args) {
        webApplicationType = WebApplicationType.REACTIVE
    }
}