package platformer.event.events.ui;

import platformer.event.Event;

/**
 * This event is triggered when the game is resumed from a paused state.
 * It can be used to handle any necessary updates or state changes when the game resumes.
 */
public record GameResumedEvent() implements Event {}