package platformer.event.events.lancer;

import platformer.event.Event;

import java.awt.*;

/**
 * Published when the Lancer boss teleports.
 *
 * @param location The center point of the teleport effect.
 * @param isTeleportIn True if the Lancer is arriving, false if departing.
 */
public record LancerTeleportEvent(Point location, boolean isTeleportIn) implements Event {}