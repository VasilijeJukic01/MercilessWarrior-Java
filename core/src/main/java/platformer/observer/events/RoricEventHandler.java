package platformer.observer.events;

import platformer.core.GameContext;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricPhaseManager;
import platformer.observer.EventHandler;
import platformer.observer.Publisher;
import platformer.observer.Subscriber;
import platformer.state.GameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
public class RoricEventHandler implements EventHandler, Subscriber, Publisher {

    private final GameState gameState;
    private final ScreenEffectsManager screenEffectsManager;
    private final EffectManager effectManager;
    private Roric roricInstance = null;

    // Rain Phase
    private boolean celestialAuraActive = false;

    // Bridge Phase
    private boolean isPhaseThreeActive = false;
    private final long[] phaseThreeTimings = { 94500, 95300, 96100, 97800, 99400, 101100, 101800, 102600 };
    private int phaseThreeShotIndex = 0;
    private long fightStartTime = 0;

    // Finale Phase
    private boolean isFinaleActive = false;
    private final long[] finaleFlashTimings = { 132600, 134200, 135800, 137400, 139100, 140700, 142400, 144100, 145700, 147400, 149100, 150800, 152400, 154000, 155600, 157200, 159000 };
    private int finaleFlashIndex = 0;

    private boolean isPaused = false;
    private long pauseStartTime = 0;

    private final List<Subscriber> subscribers = new ArrayList<>();

    public RoricEventHandler(GameContext context) {
        this.gameState = context.getGameState();
        this.effectManager = context.getEffectManager();
        this.screenEffectsManager = context.getScreenEffectsManager();
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
                    gameState.getRainManager().startRaining();
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
            case "JUMPED":
                if (o[1] instanceof Roric r) handleJumpEvent(r);
                break;
            case "LANDED":
                if (o[1] instanceof Roric r) handleLandEvent(r);
                break;
            case "REPOSITIONING":
                if (o[1] instanceof Roric r) handleRepositioningEvent(r);
                break;
            case "RORIC_TELEPORT_OUT":
                if (o[1] instanceof Point loc) handleRoricTeleportOut(loc);
                break;
            case "RORIC_TELEPORT_IN":
                if (o[1] instanceof Point loc) handleRoricTeleportIn(loc);
                break;
            case "BEAM_CHARGE_START":
                if (o[1] instanceof Roric r) handleBeamChargeStart(r);
                break;
            case "BEAM_CHARGE_END":
                if (o[1] instanceof Roric r) handleBeamChargeEnd(r);
                break;
            case "CELESTIAL_RAIN_START":
                if (o[1] instanceof Roric r) handleCelestialRainStart(r);
                break;
            case "CELESTIAL_RAIN_END":
                if (o[1] instanceof Roric r) handleCelestialRainEnd(r);
                break;
            case "RORIC_CLONE_SPAWN":
            case "RORIC_CLONE_DESPAWN":
                if (o[1] instanceof Point loc) handleCloneSpawn(loc);
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
                gameState.getLightManager().setCurrentAmbientAlpha(0);
                phaseThreeShotIndex++;
            }
        }
        if (celestialAuraActive && roricInstance != null) {
            effectManager.spawnDustParticles(roricInstance.getHitBox().getCenterX(), roricInstance.getHitBox().getCenterY(), 3, DustType.CELESTIAL_AURA, 0, roricInstance);
        }
        if (isFinaleActive) {
            long elapsedTime = System.currentTimeMillis() - fightStartTime;
            if (finaleFlashIndex < finaleFlashTimings.length && elapsedTime >= finaleFlashTimings[finaleFlashIndex]) {
                gameState.getLightManager().setAlphaWithFilter(0, new Color(100, 255, 120, 100));
                screenEffectsManager.triggerShake(30, 20.0);
                finaleFlashIndex++;
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
        gameState.getSpellManager().stopArrowRainTelegraph();
        if (newPhase == RoricPhaseManager.RoricPhase.VICTORY) {
            Roric roric = gameState.getEnemyManager().getRoricInstance();
            if (roric != null) roric.setAlive(false);
            notify("FIGHT_WON", "RORIC");
        }
        if (celestialAuraActive && newPhase != RoricPhaseManager.RoricPhase.STORM) {
            handleCelestialRainEnd(gameState.getEnemyManager().getRoricInstance());
        }
        isPhaseThreeActive = (newPhase == RoricPhaseManager.RoricPhase.BRIDGE);
        isFinaleActive = (newPhase == RoricPhaseManager.RoricPhase.FINALE);
        if (isPhaseThreeActive) {
            gameState.setDarkPhase(true);
            gameState.getLightManager().overrideAmbientDarkness(240);
            phaseThreeShotIndex = 0;
        }
        else if (isFinaleActive) finaleFlashIndex = 0;
        else {
            gameState.setDarkPhase(false);
            gameState.getLightManager().releaseAmbientDarkness();
        }
    }

    private void handleJumpEvent(Roric roric) {
        effectManager.spawnDustParticles(roric.getHitBox().getCenterX(), roric.getHitBox().getMaxY(), 10, DustType.IMPACT, 0, roric);
    }

    private void handleLandEvent(Roric roric) {
        effectManager.spawnDustParticles(roric.getHitBox().getCenterX(), roric.getHitBox().getMaxY(), 15, DustType.IMPACT, 0, roric);
    }

    private void handleRepositioningEvent(Roric roric) {
        Random rand = new Random();
        double spreadFactor = 0.6;
        double xOffset = (rand.nextDouble() - 0.5) * (roric.getHitBox().getWidth() * spreadFactor);
        double yOffset = (rand.nextDouble() - 0.5) * (roric.getHitBox().getHeight() * spreadFactor);
        effectManager.spawnDustParticles(roric.getHitBox().getCenterX() + xOffset, roric.getHitBox().getCenterY() + yOffset, 2, DustType.ETHEREAL_DASH, 0, roric);
    }

    private void handleRoricTeleportOut(Point location) {
        effectManager.spawnDustParticles(location.getX(), location.getY(), 50, DustType.RORIC_TELEPORT_OUT, 0, null);
    }

    private void handleRoricTeleportIn(Point location) {
        effectManager.spawnDustParticles(location.getX(), location.getY(), 40, DustType.RORIC_TELEPORT_IN, 0, null);
    }

    private void handleBeamChargeStart(Roric roric) {
        effectManager.spawnDustParticles(roric.getHitBox().getCenterX(), roric.getHitBox().getCenterY(), 60, DustType.RORIC_BEAM_CHARGE, 0, roric);
    }

    private void handleBeamChargeEnd(Roric roric) {
        effectManager.clearParticlesByType(roric, DustType.RORIC_BEAM_CHARGE);
    }

    private void handleCelestialRainStart(Roric roric) {
        this.celestialAuraActive = true;
        this.roricInstance = roric;
    }

    private void handleCelestialRainEnd(Roric roric) {
        this.celestialAuraActive = false;
        effectManager.clearParticlesByType(roric, DustType.CELESTIAL_AURA);
        this.roricInstance = null;
    }

    private void handleCloneSpawn(Point location) {
        effectManager.spawnDustParticles(location.getX(), location.getY(), 50, DustType.RORIC_SUMMON, 0, null);
    }

    @Override
    public void pause() {
        if (fightStartTime != 0 && !isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void unpause() {
        if (fightStartTime != 0 && isPaused) {
            long pauseDuration = System.currentTimeMillis() - pauseStartTime;
            fightStartTime += pauseDuration;
            isPaused = false;
            pauseStartTime = 0;
        }
    }

    @Override
    public void reset() {
        isPhaseThreeActive = false;
        isFinaleActive = false;
        phaseThreeShotIndex = 0;
        finaleFlashIndex = 0;
        fightStartTime = 0;
        celestialAuraActive = false;
        roricInstance = null;
        isPaused = false;
        pauseStartTime = 0;
        gameState.getLightManager().releaseAmbientDarkness();
    }

    // Observer
    @Override
    public void addSubscriber(Subscriber s) {
        if (s != null && !subscribers.contains(s)) {
            subscribers.add(s);
        }
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        subscribers.remove(s);
    }

    @Override
    public <T> void notify(T... o) {
        for (Subscriber s : subscribers) {
            s.update(o);
        }
    }
}