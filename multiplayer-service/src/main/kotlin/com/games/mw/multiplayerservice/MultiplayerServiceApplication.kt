package com.games.mw.multiplayerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class MultiplayerServiceApplication

fun main(args: Array<String>) {
	runApplication<MultiplayerServiceApplication>(*args)
}
