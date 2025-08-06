package platformer.event.events.ui;

import platformer.event.Event;

/**
 * This event is triggered when the game is paused.
 * It can be used to handle any necessary updates or state changes when the game is paused.
 */
public record GamePausedEvent() implements Event {}