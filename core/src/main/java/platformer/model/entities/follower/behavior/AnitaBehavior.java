package platformer.model.entities.follower.behavior;

import platformer.animation.Anim;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.follower.Follower;
import platformer.model.entities.player.Player;
import platformer.physics.CollisionDetector;

import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.TILES_SIZE;

public class AnitaBehavior implements FollowerBehavior {

    private final int AGGRO_RANGE = TILES_SIZE * 5;
    private final int ATTACK_RANGE = (int)(TILES_SIZE * 1.5);
    private final int COOLDOWN_TIME = 200;

    // State
    private Enemy currentTarget;
    private int cooldownTick = 0;
    private final Random rand = new Random();

    @Override
    public void update(Follower host, Player player, int[][] levelData, List<Enemy> enemies) {
        if (cooldownTick > 0) {
            cooldownTick--;
            // Retreat to player
            host.setMoveTarget(player.getHitBox().x);
            return;
        }

        if (host.isBusy()) return;
        if (currentTarget == null || !currentTarget.isAlive()) {
            currentTarget = findBestTarget(host, player, enemies, levelData);
        }

        // Fight
        if (currentTarget != null) {
            double dist = Math.abs(currentTarget.getHitBox().x - host.getHitBox().x);
            if (currentTarget.getHitBox().x > host.getHitBox().x) host.setDirection(Direction.RIGHT);
            else host.setDirection(Direction.LEFT);
            if (dist <= ATTACK_RANGE) {
                host.setMoveTarget(host.getHitBox().x);
                host.requestAttack();
                setRandomAttackAnim(host);
                cooldownTick = COOLDOWN_TIME;
            }
            // Chase Enemy
            else host.setMoveTarget(currentTarget.getHitBox().x);
        }
        // Follow
        else {
            host.setMoveTarget(player.getHitBox().x);
        }
    }

    @Override
    public void onAttackFrame(Follower host, List<Enemy> enemies) {
        boolean isWindAttack = host.getEntityState() == Anim.SPELL_1;

        for (Enemy e : enemies) {
            if (e.isAlive() && e.getEnemyAction() != Anim.DEATH && host.getAttackBox().intersects(e.getHitBox())) {
                e.hit(1, false, true);
                if (e.getHitBox().x > host.getHitBox().x) e.setPushDirection(Direction.RIGHT);
                else e.setPushDirection(Direction.LEFT);

                if (isWindAttack) e.freeze(100);
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

    private void setRandomAttackAnim(Follower host) {
        int r = rand.nextInt(20);
        if (r < 9) host.setEnemyActionNoReset(Anim.ATTACK_1);
        else if (r < 18) host.setEnemyActionNoReset(Anim.ATTACK_2);
        else host.setEnemyActionNoReset(Anim.SPELL_1);
    }
}