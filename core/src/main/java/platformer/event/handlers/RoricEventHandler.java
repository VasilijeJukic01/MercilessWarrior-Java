package platformer.event.handlers;

import platformer.core.GameContext;
import platformer.event.events.roric.RoricCloneEvent;
import platformer.event.events.roric.RoricEffectEvent;
import platformer.event.events.roric.RoricPhaseChangeEvent;
import platformer.event.events.roric.RoricTeleportEvent;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricPhaseManager;
import platformer.event.EventHandler;
import platformer.state.types.GameState;

import java.awt.*;
import java.util.Random;

/**
 * Handles all event logic for the {@link Roric} boss fight, including phase transitions and time-based attacks.
 * <p>
 * This class is the central choreographer for the Roric encounter's pacing and special effects.
 * <p>
 * Crucially, it also implements {@link EventHandler} to manage its own internal timers. The {@code continuousUpdate}
 * method checks the elapsed fight time to trigger precisely timed effects, most notably the rapid-fire screen flashes during
 * the 'Bridge' phase. This decouples the time-sensitive fight script from the boss's core AI logic.
 *
 * @see Roric
 * @see RoricPhaseManager
 * @see GameState
 * @see EventHandler
 */
public class RoricEventHandler implements EventHandler {

    private final GameContext context;
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

    public RoricEventHandler(GameContext context) {
        this.context = context;
        this.effectManager = context.getEffectManager();
        this.screenEffectsManager = context.getScreenEffectsManager();
    }

    public void onPhaseChange(RoricPhaseChangeEvent event) {
        Roric roric = context.getEnemyManager().getRoricInstance();
        if (roric != null) {
            if (this.fightStartTime == 0 && roric.getPhaseManager().getFightStartTime() != 0) {
                this.fightStartTime = roric.getPhaseManager().getFightStartTime();
            }
            roric.getAttackHandler().interruptAndIdle();
        }
        handlePhaseChange(event.newPhase());
    }

    public void onRoricTeleport(RoricTeleportEvent event) {
        if (event.isTeleportIn()) {
            effectManager.spawnDustParticles(event.location().getX(), event.location().getY(), 40, DustType.RORIC_TELEPORT_IN, 0, null);
        }
        else {
            effectManager.spawnDustParticles(event.location().getX(), event.location().getY(), 50, DustType.RORIC_TELEPORT_OUT, 0, null);
        }
    }

    public void onRoricClone(RoricCloneEvent event) {
        effectManager.spawnDustParticles(event.location().getX(), event.location().getY(), 50, DustType.RORIC_SUMMON, 0, null);
    }

    public void onRoricEffect(RoricEffectEvent event) {
        Roric roric = event.roric();
        switch (event.effectType()) {
            case JUMP -> handleJumpEvent(roric);
            case LAND -> handleLandEvent(roric);
            case REPOSITIONING -> handleRepositioningEvent(roric);
            case BEAM_CHARGE_START -> handleBeamChargeStart(roric);
            case BEAM_CHARGE_END -> handleBeamChargeEnd(roric);
            case CELESTIAL_RAIN_START -> handleCelestialRainStart(roric);
            case CELESTIAL_RAIN_END -> handleCelestialRainEnd(roric);
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
                context.getLightManager().setCurrentAmbientAlpha(0);
                phaseThreeShotIndex++;
            }
        }
        if (celestialAuraActive && roricInstance != null) {
            effectManager.spawnDustParticles(roricInstance.getHitBox().getCenterX(), roricInstance.getHitBox().getCenterY(), 3, DustType.CELESTIAL_AURA, 0, roricInstance);
        }
        if (isFinaleActive) {
            long elapsedTime = System.currentTimeMillis() - fightStartTime;
            if (finaleFlashIndex < finaleFlashTimings.length && elapsedTime >= finaleFlashTimings[finaleFlashIndex]) {
                context.getLightManager().setAlphaWithFilter(0, new Color(100, 255, 120, 100));
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
        if (newPhase == RoricPhaseManager.RoricPhase.INTRO) {
            context.getRainManager().startRaining();
        }
        context.getSpellManager().stopArrowRainTelegraph();
        if (newPhase == RoricPhaseManager.RoricPhase.VICTORY) {
            Roric roric = context.getEnemyManager().getRoricInstance();
            if (roric != null) roric.setAlive(false);
        }
        if (celestialAuraActive && newPhase != RoricPhaseManager.RoricPhase.STORM) {
            handleCelestialRainEnd(context.getEnemyManager().getRoricInstance());
        }
        isPhaseThreeActive = (newPhase == RoricPhaseManager.RoricPhase.BRIDGE);
        isFinaleActive = (newPhase == RoricPhaseManager.RoricPhase.FINALE);
        if (isPhaseThreeActive) {
            context.getGameState().setDarkPhase(true);
            context.getLightManager().overrideAmbientDarkness(240);
            phaseThreeShotIndex = 0;
        }
        else if (isFinaleActive) finaleFlashIndex = 0;
        else {
            context.getGameState().setDarkPhase(false);
            context.getLightManager().releaseAmbientDarkness();
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
        context.getLightManager().releaseAmbientDarkness();
    }
}