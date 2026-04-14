package platformer.event.handlers;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.BossDefeatedEvent;
import platformer.event.events.roric.RoricCloneEvent;
import platformer.event.events.roric.RoricEffectEvent;
import platformer.event.events.roric.RoricPhaseChangeEvent;
import platformer.event.events.roric.RoricTeleportEvent;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricClone;
import platformer.model.entities.enemies.boss.roric.RoricPhaseManager;
import platformer.event.EventHandler;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.physics.CollisionDetector;
import platformer.state.types.GameState;
import platformer.state.types.PlayingState;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

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

    // Victory Phase
    private SequenceState currSequence = SequenceState.INACTIVE;
    private int sequenceTimer = 0;
    private float ogMusicVol = 0;
    private boolean dialogueOpened = false;

    private static final int HOLD_DURATION = 80;
    private static final int FADE_DURATION = 350;

    /**
     * Handles the state machine for Roric's victory cinematic.
     * <p>
     * The cinematic follows a Hold & Fade pattern:
     * BURST_HOLD: Triggers a green wash to obscure the screen while the boss entity is swapped for the NPC entity. <br>
     * FADE_OUT: Gradually reduces the music volume and the green wash alpha to reveal the environment and the grounded NPC. <br>
     * AWAIT_DIALOGUE: Triggers the dialogue system and waits for the player to finish reading before notifying the global game flow. <br>
     */
    private enum SequenceState {
        INACTIVE, BURST_HOLD, FADE_OUT, AWAIT_DIALOGUE
    }

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
        if (currSequence != SequenceState.INACTIVE) {
            processVictorySequence();
            return;
        }
        // Normal Mechanics
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
            if (roric != null) {
                roric.getAttackHandler().interruptAndIdle();
                roric.setEnemyActionNoReset(Anim.IDLE);
            }
            startDefeatSequence();
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

    // Ending Sequence
    private void startDefeatSequence() {
        context.getLightManager().setAlphaWithFilter(0, new Color(0, 255, 100, 255));
        screenEffectsManager.triggerShake(HOLD_DURATION + FADE_DURATION, 20.0);
        Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_1);

        context.getRainManager().stopRaining();
        context.getProjectileManager().reset();
        context.getSpellManager().reset();
        for (Enemy e : context.getEnemyManager().getAllEnemies()) {
            if (e instanceof RoricClone) ((RoricClone) e).kill();
        }

        Roric roricBoss = context.getEnemyManager().getRoricInstance();
        roricBoss.setAlive(false);
        context.getGameState().getBossInterface().reset();

        ogMusicVol = Audio.getInstance().getAudioPlayer().getMusicVolume();
        currSequence = SequenceState.BURST_HOLD;
        sequenceTimer = 0;
    }

    private void processVictorySequence() {
        sequenceTimer++;
        if (currSequence == SequenceState.BURST_HOLD) {
            context.getLightManager().setAlphaWithFilter(0, new Color(0, 255, 100, 255));
            if (sequenceTimer >= HOLD_DURATION) {
                currSequence = SequenceState.FADE_OUT;
                sequenceTimer = 0;
            }
        }
        else if (currSequence == SequenceState.FADE_OUT) {
            float progress = (float) sequenceTimer / FADE_DURATION;
            float fadeVol = ogMusicVol * (1.0f - progress);
            Audio.getInstance().getAudioPlayer().setMusicVolume(Math.max(0, fadeVol));

            int greenAlpha = (int)(255 * (1.0f - progress));
            context.getLightManager().setAlphaWithFilter(0, new Color(0, 255, 100, greenAlpha));

            if (sequenceTimer >= FADE_DURATION) {
                Audio.getInstance().getAudioPlayer().setMusicVolume(ogMusicVol);
                Audio.getInstance().getAudioPlayer().stopSong();
                spawnRoricNpcForDialogue();
                currSequence = SequenceState.AWAIT_DIALOGUE;
                dialogueOpened = false;
            }
        }
        else if (currSequence == SequenceState.AWAIT_DIALOGUE) {
            processDialogueWait();
        }
    }

    /**
     * Orchestrates the transition from the Boss Entity to a Npc.
     * <p>
     * Uses {@link CollisionDetector#getGroundY} to ensure the NPC is placed on solid ground.
     * Synchronizes the appearance of the NPC with the end of the visual fade-out to create a reveal effect.
     */
    private void spawnRoricNpcForDialogue() {
        Player player = context.getPlayer();
        int offsetX = (int) (120 * SCALE);
        int spawnX = (player.getFlipSign() == 1) ? (int)player.getHitBox().x + offsetX : (int)player.getHitBox().x - offsetX;

        Npc roricNpc = new Npc(ObjType.NPC, spawnX, 0, NpcType.RORIC);
        double floorY = CollisionDetector.getGroundY(roricNpc.getHitBox().getCenterX(), player.getHitBox().y, context.getLevelManager().getCurrentLevel().getLvlData());
        roricNpc.getHitBox().y = floorY - roricNpc.getHitBox().height;

        if (player.getHitBox().x < roricNpc.getHitBox().x) roricNpc.setDirection(Direction.RIGHT);
        else roricNpc.setDirection(Direction.LEFT);

        EventBus.getInstance().publish(new RoricTeleportEvent(new Point((int)roricNpc.getHitBox().getCenterX(), (int)roricNpc.getHitBox().getCenterY()), true));
        roricNpc.increaseDialogueIndicator();
        context.getObjectManager().addGameObject(roricNpc);
        context.getDialogueManager().activateDialogue("NpcRoric", roricNpc);
    }

    private void processDialogueWait() {
        PlayingState currentState = context.getGameState().getActiveState();
        if (currentState == PlayingState.DIALOGUE) {
            dialogueOpened = true;
        }
        else if (dialogueOpened) {
            currSequence = SequenceState.INACTIVE;
            Npc roricNpc = context.getObjectManager().getNpcByType(NpcType.RORIC);
            if (roricNpc != null) {
                EventBus.getInstance().publish(new RoricTeleportEvent(new Point((int)roricNpc.getHitBox().getCenterX(), (int)roricNpc.getHitBox().getCenterY()), false));
                roricNpc.setAlive(false);
            }
            EventBus.getInstance().publish(new BossDefeatedEvent(context.getEnemyManager().getRoricInstance()));
        }
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
        currSequence = SequenceState.INACTIVE;
        sequenceTimer = 0;
        dialogueOpened = false;
        context.getLightManager().releaseAmbientDarkness();
    }
}