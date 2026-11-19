package platformer.model.entities.follower.behavior;

import platformer.animation.Anim;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.follower.Follower;
import platformer.model.entities.player.Player;
import platformer.physics.CollisionDetector;

import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;

/**
 * Vanguard combat AI for Sir Dejanovic as a follower.
 * Uses heavy greatsword cleaves (ATTACK_1) and blue arcane flame surges (ATTACK_2).
 */
public class DejanovicBehavior implements FollowerBehavior {

    private static final int AGGRO_RANGE = TILES_SIZE * 6;
    private static final int ATTACK_RANGE = (int) (TILES_SIZE * 1.65);
    private static final int COOLDOWN_TIME = 180;
    private static final int LEASH_RANGE = TILES_SIZE * 11;

    private Enemy currentTarget;
    private int cooldownTick = 0;
    private final Random rand = new Random();

    @Override
    public void update(Follower host, Player player, int[][] levelData, List<Enemy> enemies) {
        double distToPlayerX = Math.abs(player.getHitBox().x - host.getHitBox().x);
        double distToPlayerY = Math.abs(player.getHitBox().y - host.getHitBox().y);
        if (distToPlayerX > LEASH_RANGE || distToPlayerY > TILES_SIZE * 5) {
            currentTarget = null;
            cooldownTick = 0;
            host.setMoveTarget(player.getHitBox().x);
            return;
        }
        if (cooldownTick > 0) {
            cooldownTick--;
            host.setMoveTarget(host.getHitBox().x);
            if (currentTarget != null && currentTarget.isAlive()) {
                if (currentTarget.getHitBox().x > host.getHitBox().x) host.setDirection(Direction.RIGHT);
                else host.setDirection(Direction.LEFT);
            }
            return;
        }

        if (host.isBusy()) return;
        if (currentTarget == null || !currentTarget.isAlive()
                || Math.abs(currentTarget.getHitBox().x - host.getHitBox().x) > AGGRO_RANGE * 1.5) {
            currentTarget = findBestTarget(host, player, enemies, levelData);
        }

        if (currentTarget != null) {
            double dist = Math.abs(currentTarget.getHitBox().x - host.getHitBox().x);
            if (currentTarget.getHitBox().x > host.getHitBox().x) host.setDirection(Direction.RIGHT);
            else host.setDirection(Direction.LEFT);

            if (dist <= ATTACK_RANGE) {
                host.setMoveTarget(host.getHitBox().x);
                host.requestAttack();
                host.setEnemyActionNoReset(rand.nextBoolean() ? Anim.ATTACK_1 : Anim.ATTACK_2);
                cooldownTick = COOLDOWN_TIME + rand.nextInt(45);
            }
            else host.setMoveTarget(currentTarget.getHitBox().x);
        }
        else host.setMoveTarget(player.getHitBox().x);
    }

    @Override
    public void onAttackFrame(Follower host, int animIndex, List<Enemy> enemies) {
        if (animIndex == 4) {
            boolean isRadiantSurge = (host.getEntityState() == Anim.ATTACK_2);
            if (isRadiantSurge) host.getAudioHandler().playSpellAttack();
            else host.getAudioHandler().playMeleeAttack();

            host.performAttackLunge((isRadiantSurge ? 8 : 5) * SCALE, isRadiantSurge);
            applyDamageToEnemies(host, enemies, isRadiantSurge ? 6.0 : 4.0, isRadiantSurge);
        }
    }

    private void applyDamageToEnemies(Follower host, List<Enemy> enemies, double damage, boolean applyRadiantStagger) {
        if (enemies == null) return;
        for (Enemy e : enemies) {
            if (e.isAlive() && e.getEnemyAction() != Anim.DEATH && host.getAttackBox().intersects(e.getHitBox())) {
                if (e.getEnemyAction() == Anim.BLOCK) continue;
                e.hit(damage, false, false);
                if (e.getHitBox().x > host.getHitBox().x) e.setPushDirection(Direction.RIGHT);
                else e.setPushDirection(Direction.LEFT);
                if (applyRadiantStagger) {
                    e.freeze(75);
                }
            }
        }
    }

    private Enemy findBestTarget(Follower host, Player player, List<Enemy> enemies, int[][] levelData) {
        Enemy best = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double distToPlayer = Math.abs(e.getHitBox().x - player.getHitBox().x);
            if (distToPlayer > AGGRO_RANGE) continue;
            if (Math.abs(e.getHitBox().y - host.getHitBox().y) > TILES_SIZE * 1.5) continue;
            if (!isPathSafe(host, e, levelData)) continue;
            if (distToPlayer < minDist) {
                minDist = distToPlayer;
                best = e;
            }
        }
        return best;
    }

    private boolean isPathSafe(Follower host, Enemy target, int[][] levelData) {
        int startX = (int) (host.getHitBox().x / TILES_SIZE);
        int endX = (int) (target.getHitBox().x / TILES_SIZE);
        int y = (int) ((host.getHitBox().y + host.getHitBox().height + 5) / TILES_SIZE);
        int step = (startX < endX) ? 1 : -1;
        for (int i = startX; i != endX; i += step) {
            if (i < 0 || i >= levelData.length) return false;
            if (!CollisionDetector.isTileSolid(i, y, levelData)) return false;
        }
        return true;
    }
}