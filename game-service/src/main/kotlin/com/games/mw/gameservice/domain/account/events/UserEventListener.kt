package com.games.mw.gameservice.domain.account.events

import com.games.mw.events.UserCreated
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.account.settings.model.Settings
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

/**
 * Listens for user-related events from Kafka and processes them.
 * This class acts as the consumer part of an event-driven Saga pattern.
 *
 * @property settingsService Service used to create game settings for new users.
 */
@Service
class UserEventListener(
    private val settingsService: SettingsService
) {

    private val logger = LoggerFactory.getLogger(UserEventListener::class.java)

    /**
     * Handles the UserCreated event.
     *
     * This method creates the corresponding default game settings for that user.
     * The listener is idempotent; it checks if settings already exist for the user ID to safely handle potential duplicate message deliveries from Kafka.
     *
     * If processing fails, the configured ErrorHandler and Retry policies will take over.
     *
     * @param event The deserialized UserCreatedEvent from the 'user_events' topic.
     */
    @KafkaListener(topics = ["user_events"], containerFactory = "userCreatedKafkaListenerContainerFactory")
    fun handleUserCreatedEvent(event: UserCreated) {
        logger.info("Received UserCreatedEvent for userId: ${event.userId}, username: ${event.username}")

        val existingSettings = settingsService.getSettingsByUserId(event.userId)
        if (existingSettings.isRight()) {
            logger.warn("Settings for userId ${event.userId} already exist. Ignoring duplicate event.")
            return
        }

        settingsService.insertSettings(Settings(userId = event.userId)).fold(
            { error -> logger.error("Failed to create settings for userId ${event.userId}: $error") },
            { newSettings -> logger.info("Successfully created settings for userId ${event.userId} with settingsId ${newSettings.id}") }
        )
    }

}