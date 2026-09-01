package platformer.event.events.effects;

import platformer.event.Event;

/**
 * A generic event to request a hit-stop (screen freeze) effect.
 *
 * @param frames The duration of the freeze in engine ticks.
 */
public record HitStopEvent(int frames) implements Event {}