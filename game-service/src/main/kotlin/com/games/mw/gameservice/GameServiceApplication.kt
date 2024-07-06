package com.games.mw.gameservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class GameServiceApplication

fun main(args: Array<String>) {
    runApplication<GameServiceApplication>(*args)
}
