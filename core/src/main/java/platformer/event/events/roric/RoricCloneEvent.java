package platformer.event.events.roric;

import platformer.event.Event;

import java.awt.*;

/**
 * Published when a Roric clone is spawned or despawned.
 *
 * @param location The location of the spawn/despawn effect.
 */
public record RoricCloneEvent(Point location) implements Event {}