package platformer.event.events;

import platformer.event.Event;
import platformer.model.entities.enemies.Enemy;

/**
 * An event that is published when an enemy has been defeated.
 *
 * @param defeatedEnemy The enemy instance that was defeated.
 */
public record EnemyDefeatedEvent(Enemy defeatedEnemy) implements Event {}