package platformer.event.events.effects;

import platformer.event.Event;

/**
 * A generic event to request a screen shake effect.
 *
 * @param duration The duration of the shake in ticks.
 * @param intensity The magnitude of the shake.
 */
public record ScreenShakeEvent(int duration, double intensity) implements Event {}