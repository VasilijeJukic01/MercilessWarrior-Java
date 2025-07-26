package platformer.model.entities.enemies.boss.roric;

import platformer.debug.DebugSettings;
import platformer.model.entities.enemies.boss.Roric;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the multi-phase structure of the Roric boss fight.
 * This class acts as a "fight choreographer," tracking the elapsed time of the battle and dictating which attacks are available
 * to Roric based on the current musical phase. It also provides phase-specific modifiers like attack speed and cooldowns.
 */
public class RoricPhaseManager {

    private final Roric roric;

    public enum RoricPhase {
        INTRO, ASSAULT, BRIDGE, STORM, FINALE, VICTORY
    }

    // Phase timings
    private static final long PHASE_2_START_TIME = 53 * 1000;
    private static final long PHASE_3_START_TIME = (int) (92.75 * 1000);
    private static final long PHASE_4_START_TIME = 106 * 1000;
    private static final long PHASE_5_START_TIME = 132 * 1000;
    private static final long PHASE_6_START_TIME = 162 * 1000;

    private RoricShuffleBag phaseOneBag;
    private RoricShuffleBag phaseTwoBag;
    private RoricShuffleBag phaseThreeBag;
    private RoricShuffleBag phaseFourBag;
    private RoricShuffleBag phaseFiveBag;

    private long fightStartTime = 0;
    private long lastSkybeamSpawnTime = 0;
    private RoricPhase currentPhase = RoricPhase.INTRO;
    private boolean isPaused = false;
    private long pauseStartTime = 0;

    private final Random random = new Random();

    public RoricPhaseManager(Roric roric) {
        this.roric = roric;
       initShuffleBag();
    }

    private void initShuffleBag() {
        List<RoricState> baseAttacks = List.of(
                RoricState.BEAM_ATTACK,
                RoricState.ARROW_RAIN,
                RoricState.ARROW_STRIKE,
                RoricState.ARROW_ATTACK
        );
        this.phaseOneBag = new RoricShuffleBag(buildAttacks(baseAttacks, RoricState.JUMPING,RoricState.ARROW_ATTACK));
        this.phaseTwoBag = new RoricShuffleBag(buildAttacks(phaseOneBag.getMasterList(), RoricState.SKYFALL_BARRAGE, RoricState.SKYFALL_BARRAGE));
        this.phaseThreeBag = new RoricShuffleBag(List.of(RoricState.ARROW_ATTACK));
        this.phaseFourBag = new RoricShuffleBag(List.of(RoricState.CELESTIAL_RAIN));
        this.phaseFiveBag = new RoricShuffleBag(buildAttacks(baseAttacks, RoricState.PHANTOM_BARRAGE, RoricState.PHANTOM_BARRAGE));
    }

    /**
     * Starts the fight timer. Should be called when the Roric fight begins.
     */
    public void startFight() {
        if (fightStartTime == 0) {
            fightStartTime = System.currentTimeMillis();
            if (DebugSettings.getInstance().isRoricDebugMode()) {
                fightStartTime -= DebugSettings.getInstance().getRoricFightStartOffsetMs();
            }
        }
    }

    /**
     * Updates the current phase based on the elapsed fight time.
     * This should be called every frame during the fight.
     */
    public void update() {
        if (fightStartTime == 0) return;
        long elapsedTime = System.currentTimeMillis() - fightStartTime;
        RoricPhase oldPhase = currentPhase;

        if (elapsedTime >= PHASE_6_START_TIME) {
            currentPhase = RoricPhase.VICTORY;
        }
        else if (elapsedTime >= PHASE_5_START_TIME) {
            currentPhase = RoricPhase.FINALE;
        }
        else if (elapsedTime >= PHASE_4_START_TIME) {
            currentPhase = RoricPhase.STORM;
        }
        else if (elapsedTime >= PHASE_3_START_TIME) {
            currentPhase = RoricPhase.BRIDGE;
        }
        else if (elapsedTime >= PHASE_2_START_TIME) {
            currentPhase = RoricPhase.ASSAULT;
        }
        else currentPhase = RoricPhase.INTRO;

        if (oldPhase != currentPhase) {
            roric.notify("PHASE_CHANGE", currentPhase);
        }
        if (currentPhase == RoricPhase.FINALE) {
            long now = System.currentTimeMillis();
            if (now - lastSkybeamSpawnTime >= 1000) {
                roric.notify("SPAWN_RANDOM_SKYBEAM", null);
                lastSkybeamSpawnTime = now;
            }
        }
        else lastSkybeamSpawnTime = 0;
    }

    /**
     * Draws a random, fair attack for Roric to perform based on the current phase's Shuffle Bag.
     *
     * @return A {@link RoricState} representing the chosen attack.
     */
    public RoricState chooseNextAttack() {
        return switch (currentPhase) {
            case INTRO -> phaseOneBag.draw();
            case ASSAULT -> phaseTwoBag.draw();
            case BRIDGE -> phaseThreeBag.draw();
            case STORM -> phaseFourBag.draw();
            case FINALE -> phaseFiveBag.draw();
            case VICTORY -> RoricState.IDLE;
        };
    }

    /**
     * Checks if the aerial attack should drop a trap in the current phase.
     *
     * @return true if a trap should be spawned, false otherwise.
     */
    public boolean shouldAerialAttackDropTrap() {
        if (currentPhase == RoricPhase.ASSAULT) {
            return random.nextBoolean();
        }
        else if (currentPhase == RoricPhase.FINALE) return true;
        return false;
    }

    /**
     * A helper method to build an attack list by combining a base list with additional attacks.
     *
     * @param base The base list of RoricState attacks.
     * @param additions A varargs array of additional RoricState attacks to add.
     * @return A new, combined list of attacks.
     */
    private static List<RoricState> buildAttacks(List<RoricState> base, RoricState... additions) {
        return Stream.concat(base.stream(), Stream.of(additions)).collect(Collectors.toList());
    }

    /**
     * Gets the global cooldown multiplier for the current phase.
     * This can be used to speed up the fight in later phases.
     *
     * @return A multiplier (e.g., 1.0 for normal, 0.60 for 40% faster).
     */
    public double getCooldownModifier() {
        if (currentPhase == RoricPhase.FINALE) return 0.6;
        return 1.0;
    }

    /**
     * Gets the animation speed modifier for the current phase.
     * A value less than 1.0 means faster animations.
     *
     * @return A multiplier for animation speed (e.g., 0.6 for 40% faster).
     */
    public double getAnimationSpeedModifier() {
        if (currentPhase == RoricPhase.BRIDGE) return 0.8;
        return 1.0;
    }

    /**
     * Gets the arrow speed multiplier for the current phase.
     * A value greater than 1.0 means faster arrows.
     *
     * @return A multiplier for arrow speed (e.g., 1.5 for 50% faster).
     */
    public double getArrowSpeedMultiplier() {
        if (currentPhase == RoricPhase.BRIDGE) return 1.8;
        return 1.0;
    }

    /**
     * Pauses the fight timer if the fight is active.
     */
    public void pauseFightTimer() {
        if (fightStartTime != 0 && !isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Unpauses the fight timer and adjusts the start time to account for the pause duration.
     */
    public void unpauseFightTimer() {
        if (fightStartTime != 0 && isPaused) {
            long pauseDuration = System.currentTimeMillis() - pauseStartTime;
            fightStartTime += pauseDuration;
            isPaused = false;
            pauseStartTime = 0;
        }
    }

    /**
     * Resets the manager to its initial state for a new fight.
     */
    public void reset() {
        fightStartTime = 0;
        currentPhase = RoricPhase.INTRO;
        isPaused = false;
        pauseStartTime = 0;
    }

    // Getters
    public long getFightStartTime() {
        return fightStartTime;
    }

    public RoricPhase getCurrentPhase() {
        return currentPhase;
    }
}
