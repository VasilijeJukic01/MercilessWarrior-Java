package platformer.event.events.lancer;

import platformer.event.Event;
import platformer.model.entities.enemies.boss.Lancer;

/**
 * Published to control the Lancer's special aura effect.
 *
 * @param lancer The Lancer instance.
 * @param shouldBeActive True to spawn the aura, false to clear it.
 */
public record LancerAuraEvent(Lancer lancer, boolean shouldBeActive) implements Event {}