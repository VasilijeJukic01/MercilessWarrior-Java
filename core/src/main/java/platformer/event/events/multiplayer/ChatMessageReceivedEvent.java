package platformer.event.events.multiplayer;

import platformer.event.Event;

/**
 * Published on the client's EventBus when a chat message is received from the server.
 */
public record ChatMessageReceivedEvent(String username, String message) implements Event {}