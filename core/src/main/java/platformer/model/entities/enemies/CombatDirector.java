package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;

import java.util.*;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;

/**
 * Coordinates multi-enemy combat encounters:
 * 1. Allows only one enemy to perform a melee attack at a time, with a recovery window between attackers.
 * 2. Gives each waiting enemy independent footwork (slowly stepping in, pausing in IDLE, stepping back) so they look like live fighters shifting their weight rather than a static queue.
 */
public class CombatDirector {

    private static final int POST_ATTACK_BREATHING_TICKS = 75;
    private static final int RANGED_COOLDOWN_TICKS = 160;
    private static final double SAME_FLOOR_TOLERANCE = TILES_SIZE * 1.75;
    private static final double ENGAGEMENT_RANGE = TILES_SIZE * 7.0;

    private static class FootworkState {
        double currentOffset;
        double targetOffset;
        int pauseTicks;
    }

    private Enemy activeMeleeAttacker = null;
    private Enemy lastMeleeAttacker = null;
    private int meleeBreathingTimer = 0;
    private int rangedCooldownTimer = 0;

    private final Map<Enemy, Double> assignedSpacing = new HashMap<>();
    private final Map<Enemy, FootworkState> footworkMap = new HashMap<>();
    private final Set<Enemy> waitingEnemies = new HashSet<>();
    private final Random rand = new Random();

    public void update(List<Enemy> enemies, Player player, Entity follower) {
        if (meleeBreathingTimer > 0) meleeBreathingTimer--;
        if (rangedCooldownTimer > 0) rangedCooldownTimer--;

        validateActiveAttacker();
        assignSlots(enemies, player, follower);
    }

    private void validateActiveAttacker() {
        if (activeMeleeAttacker == null) return;

        if (!activeMeleeAttacker.isAlive() || activeMeleeAttacker.isStunned() || activeMeleeAttacker.getEnemyAction() == Anim.HIT || activeMeleeAttacker.getEnemyAction() == Anim.DEATH) {
            lastMeleeAttacker = activeMeleeAttacker;
            activeMeleeAttacker = null;
            meleeBreathingTimer = Math.max(meleeBreathingTimer, 40);
            return;
        }

        if (!activeMeleeAttacker.isAttacking()) {
            lastMeleeAttacker = activeMeleeAttacker;
            activeMeleeAttacker = null;
            meleeBreathingTimer = POST_ATTACK_BREATHING_TICKS;
        }
    }

    private void assignSlots(List<Enemy> enemies, Player player, Entity follower) {
        assignedSpacing.clear();
        waitingEnemies.clear();
        if (player == null) return;

        List<Enemy> leftSide = new ArrayList<>();
        List<Enemy> rightSide = new ArrayList<>();

        for (Enemy e : enemies) {
            if (!e.isAlive() || e.isBoss() || e.getEnemyAction() == Anim.DEATH || e.getEnemyAction() == Anim.HIDE) {
                continue;
            }
            Entity target = e.getCloseTarget(player, follower);
            double dx = e.getHitBox().getCenterX() - target.getHitBox().getCenterX();
            double dy = Math.abs(e.getHitBox().getCenterY() - target.getHitBox().getCenterY());
            if (Math.abs(dx) <= ENGAGEMENT_RANGE && dy <= SAME_FLOOR_TOLERANCE) {
                if (dx < 0) leftSide.add(e);
                else rightSide.add(e);
            }
        }

        Comparator<Enemy> priority = Comparator
                .comparing((Enemy e) -> e != activeMeleeAttacker)
                .thenComparing((Enemy e) -> e.isStunned() || e.getEnemyAction() == Anim.HIT)
                .thenComparing((Enemy e) -> e.getCooldown()[Cooldown.ATTACK.ordinal()] > 0)
                .thenComparing((Enemy e) -> e == lastMeleeAttacker)
                .thenComparingDouble(e -> Math.abs(e.getHitBox().getCenterX() - player.getHitBox().getCenterX()));

        leftSide.sort(priority);
        rightSide.sort(priority);

        assignSideSpacing(leftSide);
        assignSideSpacing(rightSide);
    }

    private void assignSideSpacing(List<Enemy> side) {
        for (int i = 0; i < side.size(); i++) {
            Enemy e = side.get(i);
            double baseMin = e.getDefinition().getMinSpacing();

            if (i == 0 && (e == activeMeleeAttacker || (activeMeleeAttacker == null && meleeBreathingTimer == 0))) {
                assignedSpacing.put(e, baseMin);
            }
            else {
                waitingEnemies.add(e);
                double footworkOffset = updateFootwork(e, i);
                assignedSpacing.put(e, baseMin + footworkOffset);
            }
        }
    }

    private double updateFootwork(Enemy e, int slotIndex) {
        double minBand = (12.0 + (slotIndex - 1) * 22.0) * SCALE;
        double maxBand = minBand + (24.0 * SCALE);

        FootworkState state = footworkMap.computeIfAbsent(e, k -> {
            FootworkState fs = new FootworkState();
            fs.currentOffset = minBand + rand.nextDouble() * (maxBand - minBand);
            fs.targetOffset = fs.currentOffset;
            fs.pauseTicks = 25 + rand.nextInt(55);
            return fs;
        });

        if (state.pauseTicks > 0) {
            state.pauseTicks--;
            if (state.pauseTicks == 0) {
                state.targetOffset = minBand + rand.nextDouble() * (maxBand - minBand);
            }
        }
        else {
            double diff = state.targetOffset - state.currentOffset;
            double speed = e.getDefinition().getScaledWalkSpeed() * 0.70;
            if (Math.abs(diff) <= speed) {
                state.currentOffset = state.targetOffset;
                state.pauseTicks = 45 + rand.nextInt(75);
            }
            else state.currentOffset += Math.signum(diff) * speed;
        }
        return state.currentOffset;
    }

    public boolean requestMeleeAttack(Enemy enemy) {
        if (enemy.isBoss()) return true;
        if (activeMeleeAttacker == enemy) return true;
        if (activeMeleeAttacker != null || meleeBreathingTimer > 0) return false;

        activeMeleeAttacker = enemy;
        waitingEnemies.remove(enemy);
        return true;
    }

    public boolean requestRangedAttack(Enemy enemy) {
        if (enemy.isBoss()) return true;
        if (activeMeleeAttacker != null || rangedCooldownTimer > 0 || meleeBreathingTimer > 0) return false;

        rangedCooldownTimer = RANGED_COOLDOWN_TICKS;
        meleeBreathingTimer = 55;
        return true;
    }

    public void releaseToken(Enemy enemy) {
        if (activeMeleeAttacker == enemy) {
            lastMeleeAttacker = enemy;
            activeMeleeAttacker = null;
            meleeBreathingTimer = POST_ATTACK_BREATHING_TICKS;
        }
    }

    public boolean isWaitingGuard(Enemy enemy) {
        return waitingEnemies.contains(enemy);
    }

    public double getAssignedMinSpacing(Enemy enemy, double defaultMinSpacing) {
        return assignedSpacing.getOrDefault(enemy, defaultMinSpacing);
    }

    public void reset() {
        activeMeleeAttacker = null;
        lastMeleeAttacker = null;
        meleeBreathingTimer = 0;
        rangedCooldownTimer = 0;
        assignedSpacing.clear();
        footworkMap.clear();
        waitingEnemies.clear();
    }
}