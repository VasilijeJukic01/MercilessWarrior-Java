package platformer.event.events;

import platformer.event.Event;
import platformer.model.perks.Perk;

/**
 * An event published when a player successfully unlocks or upgrades a perk.
 *
 * @param unlockedPerk The perk instance that was unlocked.
 */
public record PerkUnlockedEvent(Perk unlockedPerk) implements Event {}