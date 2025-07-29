package com.games.mw.gameservice.domain.shop.stream

import com.games.mw.events.ShopTransaction
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EventProducerService(
    private val kafkaTemplate: KafkaTemplate<String, ShopTransaction>
) {

    fun sendShopTransaction(event: ShopTransaction) {
        kafkaTemplate.send("shop_transactions", event.userId.toString(), event)
    }

}