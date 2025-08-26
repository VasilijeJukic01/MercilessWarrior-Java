package platformer.event.events;

import platformer.event.Event;

/**
 * Published just before the game state is serialized for saving.
 * Listeners can use this to ensure their data is up-to-date in the central Account object.
 */
public record PreSaveEvent() implements Event {}