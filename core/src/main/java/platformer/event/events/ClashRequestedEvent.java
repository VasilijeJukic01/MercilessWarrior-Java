package platformer.event.events;

import platformer.event.Event;
import platformer.model.combat.ClashConfig;
import platformer.model.entities.enemies.Enemy;

/**
 * Generic event published when a parry or combat trigger initiates an interactive aerial clash duel.
 */
public record ClashRequestedEvent(Enemy opponent, ClashConfig config) implements Event {}