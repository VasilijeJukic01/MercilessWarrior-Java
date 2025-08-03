package platformer.event.events.roric;

import platformer.event.Event;

import java.awt.*;

/**
 * Published when Roric or one of his clones teleports.
 *
 * @param location The center point of the teleport effect.
 * @param isTeleportIn True if arriving, false if departing.
 */
public record RoricTeleportEvent(Point location, boolean isTeleportIn) implements Event {}