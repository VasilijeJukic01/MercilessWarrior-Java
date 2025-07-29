package com.games.mw.gameservice.config.game

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ConfigurationProperties(prefix = "shop")
class ShopConfig {

    var resetInterval: Duration = Duration.ofHours(3)

    fun getCurrentResetPeriod(): Long {
        return System.currentTimeMillis() / resetInterval.toMillis()
    }
}