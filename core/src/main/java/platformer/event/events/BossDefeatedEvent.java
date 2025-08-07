package platformer.event.events;

import platformer.event.Event;
import platformer.model.entities.enemies.Enemy;

/**
 * Published when a major boss has been defeated.
 *
 * @param boss The boss entity that was defeated.
 */
public record BossDefeatedEvent(Enemy boss) implements Event {}