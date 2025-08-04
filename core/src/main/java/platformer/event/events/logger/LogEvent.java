package platformer.event.events.logger;

import platformer.debug.logger.Message;
import platformer.event.Event;

/**
 * An event published when a message needs to be logged.
 *
 * @param message The log message content.
 * @param type    The severity or type of the log message.
 */
public record LogEvent(String message, Message type) implements Event {}