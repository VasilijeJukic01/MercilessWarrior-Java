package platformer.model.entities.enemies.boss.roric;

import platformer.debug.DebugSettings;
import platformer.model.entities.enemies.boss.Roric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Manages the multi-phase structure of the Roric boss fight.
 * This class acts as a "fight choreographer," tracking the elapsed time of the battle and dictating which attacks are available
 * to Roric based on the current musical phase. It also provides phase-specific modifiers like attack speed and cooldowns.
 */
public class RoricPhaseManager {

    private final Roric roric;

    public enum RoricPhase {
        INTRO, ASSAULT, BRIDGE, STORM, FINALE
    }

    // Phase timings
    private static final long PHASE_2_START_TIME = 53 * 1000;
    private static final long PHASE_3_START_TIME = 93 * 1000;
    private static final long PHASE_4_START_TIME = 106 * 1000;
    private static final long PHASE_5_START_TIME = 132 * 1000;

    private final List<RoricState> phaseOneAttacks = Arrays.asList(
            RoricState.ARROW_ATTACK,
            RoricState.JUMPING,
            RoricState.BEAM_ATTACK,
            RoricState.ARROW_RAIN,
            RoricState.ARROW_STRIKE
    );
    private final List<RoricState> phaseTwoAttacks = new ArrayList<>(phaseOneAttacks);
    private final List<RoricState> phaseThreeAttacks = List.of(RoricState.ARROW_ATTACK);
    private final List<RoricState> phaseFourAttacks = List.of(RoricState.CELESTIAL_RAIN);
    private final List<RoricState> phaseFiveAttacks = new ArrayList<>(phaseTwoAttacks);

    private long fightStartTime = 0;
    private long lastSkybeamSpawnTime = 0;
    private RoricPhase currentPhase = RoricPhase.INTRO;

    private final Random random = new Random();

    public RoricPhaseManager(Roric roric) {
        this.roric = roric;
        phaseTwoAttacks.add(RoricState.SKYFALL_BARRAGE);
        phaseFiveAttacks.add(RoricState.PHANTOM_BARRAGE);
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

        if (elapsedTime >= PHASE_5_START_TIME) {
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
     * Selects a random, valid attack for Roric to perform based on the current phase.
     *
     * @return A {@link RoricState} representing the chosen attack.
     */
    public RoricState chooseNextAttack() {
        List<RoricState> availableAttacks = switch (currentPhase) {
            case INTRO -> phaseOneAttacks;
            case ASSAULT -> phaseTwoAttacks;
            case BRIDGE -> phaseThreeAttacks;
            case STORM -> phaseFourAttacks;
            case FINALE -> phaseFiveAttacks;
        };
        return availableAttacks.get(random.nextInt(availableAttacks.size()));
    }

    /**
     * Checks if the aerial attack should drop a trap in the current phase.
     *
     * @return true if a trap should be spawned, false otherwise.
     */
    public boolean shouldAerialAttackDropTrap() {
        if (currentPhase == RoricPhase.ASSAULT || currentPhase == RoricPhase.FINALE) {
            // TODO: Change 100% chances in finale
            return random.nextBoolean();
        }
        return false;
    }

    /**
     * Gets the global cooldown multiplier for the current phase.
     * This can be used to speed up the fight in later phases.
     *
     * @return A multiplier (e.g., 1.0 for normal, 0.75 for 25% faster).
     */
    public double getCooldownModifier() {
        if (currentPhase == RoricPhase.FINALE) return 0.7;
        return 1.0;
    }

    /**
     * Resets the manager to its initial state for a new fight.
     */
    public void reset() {
        fightStartTime = 0;
        currentPhase = RoricPhase.INTRO;
    }

    // Getters
    public long getFightStartTime() {
        return fightStartTime;
    }

    public RoricPhase getCurrentPhase() {
        return currentPhase;
    }
}
