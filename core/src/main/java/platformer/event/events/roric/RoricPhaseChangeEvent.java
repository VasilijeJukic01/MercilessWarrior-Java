package platformer.event.events.roric;

import platformer.event.Event;
import platformer.model.entities.enemies.boss.roric.RoricPhaseManager;

/**
 * Published when the Roric boss fight transitions to a new phase.
 *
 * @param newPhase The new phase of the Roric boss fight.
 */
public record RoricPhaseChangeEvent(RoricPhaseManager.RoricPhase newPhase) implements Event {}