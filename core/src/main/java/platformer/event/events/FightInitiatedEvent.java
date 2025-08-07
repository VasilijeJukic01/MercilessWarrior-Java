package platformer.event.events;

import platformer.event.Event;

/**
 * Published when the player makes a choice that initiates a major fight.
 *
 * @param bossId A unique identifier for the boss being fought (e.g., "RORIC").
 */
public record FightInitiatedEvent(String bossId) implements Event {}