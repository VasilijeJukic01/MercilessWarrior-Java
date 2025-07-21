package platformer.observer.events;

import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricPhaseManager;
import platformer.observer.EventHandler;
import platformer.observer.Subscriber;
import platformer.state.GameState;

/**
 * Handles all event logic for the {@link Roric} boss fight, including phase transitions and time-based attacks.
 * <p>
 * This class is the central choreographer for the Roric encounter's pacing and special effects.
 * It implements {@link Subscriber} to react to discrete events from Roric, such as a phase change or
 * the start of the fight.
 * <p>
 * Crucially, it also implements {@link EventHandler} to manage its own internal timers. The {@code continuousUpdate}
 * method checks the elapsed fight time to trigger precisely timed effects, most notably the rapid-fire screen flashes during
 * the 'Bridge' phase. This decouples the time-sensitive fight script from the boss's core AI logic.
 *
 * @see Roric
 * @see RoricPhaseManager
 * @see GameState
 * @see Subscriber
 * @see EventHandler
 */
public class RoricEventHandler implements EventHandler, Subscriber {

    private final GameState gameState;

    // Bridge Phase
    private boolean isPhaseThreeActive = false;
    private final long[] phaseThreeTimings = { 94500, 95300, 96100, 97800, 99400, 101100, 101800, 102600 };
    private int phaseThreeShotIndex = 0;
    private long fightStartTime = 0;

    public RoricEventHandler(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Receives event notifications from the Roric boss.
     * This method is triggered when Roric calls its {@code notify()} method.
     *
     * @param o An array of objects, typically a String event type followed by data.
     * @param <T> The type of the event parameters.
     */
    @Override
    public <T> void update(T... o) {
        if (o == null || !(o[0] instanceof String)) return;
        String eventType = (String) o[0];

        switch (eventType) {
            case "START_FIGHT":
                if (o.length > 1 && o[1] instanceof Long) {
                    this.fightStartTime = (long) o[1];
                }
                break;
            case "PHASE_CHANGE":
                if (o[1] instanceof RoricPhaseManager.RoricPhase newPhase) {
                    Roric roric = gameState.getEnemyManager().getRoricInstance();
                    if (roric != null) roric.getAttackHandler().interruptAndIdle();
                    handlePhaseChange(newPhase);
                }
                break;
            case "SPAWN_RANDOM_SKYBEAM":
                gameState.getSpellManager().spawnSkyBeam();
                break;
            case "FIRE_FAST_ARROW":
                if (o[1] instanceof Roric r) {
                    double speedMultiplier = r.getPhaseManager().getArrowSpeedMultiplier();
                    gameState.getProjectileManager().activateRoricArrow(r, speedMultiplier);
                }
                break;
        }
    }

    /**
     * Called every frame by the GameState. This method checks the elapsed fight time to trigger precisely scripted events.
     * It is primarily used to fire off the rapid succession of screen flashes during Roric's 'Bridge' (Phase 3) sequence.
     */
    @Override
    public void continuousUpdate() {
        if (isPhaseThreeActive) {
            long elapsedTime = System.currentTimeMillis() - fightStartTime;
            if (phaseThreeShotIndex < phaseThreeTimings.length && elapsedTime >= phaseThreeTimings[phaseThreeShotIndex]) {
                gameState.getLightManager().setAlpha(0);
                phaseThreeShotIndex++;
            }
        }
    }

    /**
     * Reacts to the "PHASE_CHANGE" event, updating the handler's internal state and modifying
     * the game's visual state (e.g., entering or exiting the dark phase) via GameState.
     *
     * @param newPhase The new phase of the Roric fight.
     */
    private void handlePhaseChange(RoricPhaseManager.RoricPhase newPhase) {
        isPhaseThreeActive = (newPhase == RoricPhaseManager.RoricPhase.BRIDGE);
        if (isPhaseThreeActive) {
            gameState.setDarkPhase(true);
            gameState.getLightManager().setAmbientDarkness(240);
            phaseThreeShotIndex = 0;
        }
        else {
            gameState.setDarkPhase(false);
            gameState.getLightManager().setAmbientDarkness(130);
        }
    }
}