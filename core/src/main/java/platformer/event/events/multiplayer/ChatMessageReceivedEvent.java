package platformer.event.events.multiplayer;

import platformer.event.Event;

import java.awt.*;

/**
 * Published on the client's EventBus when a chat message is received from the server.
 */
public record ChatMessageReceivedEvent(String username, String message, Color userColor) implements Event {}