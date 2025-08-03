package platformer.event.events.lancer;

import platformer.event.Event;

import java.awt.geom.Point2D;

/**
 * Published when the Lancer performs its dash-slash attack.
 *
 * @param startPos The starting position of the dash.
 * @param endPos The ending position of the dash.
 * @param hitboxHeight The vertical height of the effect area.
 */
public record LancerDashSlashEvent(Point2D.Double startPos, Point2D.Double endPos, double hitboxHeight) implements Event {}