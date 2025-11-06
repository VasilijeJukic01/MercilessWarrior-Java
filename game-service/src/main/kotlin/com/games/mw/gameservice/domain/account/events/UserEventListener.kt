package com.games.mw.gameservice.domain.account.events

import com.games.mw.events.UserCreated
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.account.settings.model.Settings
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Listens for user-related events from Kafka and processes them.
 * This class acts as the consumer part of an event-driven Saga pattern.
 *
 * @property settingsService Service used to create game settings for new users.
 * @property transactionManager The standard platform transaction manager for JPA.
 */
@Service
class UserEventListener(
    private val settingsService: SettingsService,
    private val transactionManager: PlatformTransactionManager
) {

    private val logger = LoggerFactory.getLogger(UserEventListener::class.java)
    private val transactionTemplate = TransactionTemplate(transactionManager)

    /**
     * Handles the UserCreated event.
     *
     * This method creates the corresponding default game settings for that user.
     * Logic is wrapped in a programmatic transaction using TransactionTemplate, which is the correct pattern for managing blocking JPA.
     *
     * @param event The deserialized UserCreatedEvent from the 'user_events' topic.
     */
    @KafkaListener(topics = ["user_events"], containerFactory = "userCreatedKafkaListenerContainerFactory")
    fun handleUserCreatedEvent(event: UserCreated) {
        transactionTemplate.execute { status ->
            try {
                logger.info("Received UserCreatedEvent for userId: ${event.userId}, username: ${event.username}")

                val existingSettings = settingsService.getSettingsByUserId(event.userId)
                if (existingSettings.isRight()) {
                    logger.warn("Settings for userId ${event.userId} already exist. Ignoring duplicate event.")
                    return@execute
                }

                settingsService.insertSettings(Settings(userId = event.userId)).fold(
                    { error ->
                        logger.error("Failed to create settings for userId ${event.userId}: $error. Rolling back.")
                        status.setRollbackOnly()
                    },
                    { newSettings ->
                        logger.info("Successfully created settings for userId ${event.userId} with settingsId ${newSettings.id}")
                    }
                )
            } catch (e: Exception) {
                logger.error("Uncaught exception during UserCreatedEvent processing for userId ${event.userId}. Rolling back.", e)
                status.setRollbackOnly()
                throw e
            }
        }
    }
}